package com.petshop.backend.servlet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = "/")
public class SmartPetServlet extends HttpServlet {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();
        if (path == null || path.isEmpty()) path = req.getServletPath();
        if (path == null || path.isEmpty()) path = "/";
        try {
            if ("/health/db".equals(path)) { query("SELECT 1"); json(resp, 200, Map.of("ok", true)); return; }
            if (path.startsWith("/css/")) {
                var defaultServlet = getServletContext().getNamedDispatcher("default");
                if (defaultServlet != null) { defaultServlet.forward(req, resp); return; }
            }
            if ("/api/servicos".equals(path)) { json(resp, 200, Map.of("sucesso", true, "servicos", query("SELECT codigo_servico AS id, codigo_servico, nome, preco, categoria FROM servico ORDER BY nome"))); return; }
            if ("/api/produtos".equals(path)) { json(resp, 200, query("SELECT id_produto AS codigo, nome, marca, tipo AS categoria, estoque, preco, url FROM produto ORDER BY nome")); return; }
            if ("/api/notificacoes".equals(path)) { json(resp, 200, List.of()); return; }
            if (path.startsWith("/api/fila/cliente/")) {
                String cpf=path.substring("/api/fila/cliente/".length());
                json(resp,200,query("SELECT f.id_atendimento AS id,p.nome AS nomePet,s.nome AS servico,f.status,TIMESTAMPDIFF(MINUTE,f.data_hora_entrada,CURRENT_TIMESTAMP) AS esperaMinutos FROM fila_atendimento f JOIN pet p ON p.codigo_pet=f.id_petFK JOIN servico s ON s.codigo_servico=f.id_servicoFK JOIN tutela tu ON tu.codigo_pet=p.codigo_pet WHERE tu.cpf=? ORDER BY f.data_hora_entrada DESC",cpf));return;
            }
            if ("/api/avaliacoes/pendentes".equals(path)) {
                json(resp,200,query("SELECT f.id_atendimento AS id,f.id_atendimento AS servico_id,s.nome AS servico,p.nome AS nomePet FROM fila_atendimento f JOIN servico s ON s.codigo_servico=f.id_servicoFK JOIN pet p ON p.codigo_pet=f.id_petFK LEFT JOIN avaliacao a ON a.id_atendimentoFK=f.id_atendimento WHERE f.status IN ('Concluído','Concluido','Finalizado') AND a.id_avaliacao IS NULL ORDER BY f.data_hora_entrada DESC"));return;
            }
            if (path.startsWith("/tutor/") && path.endsWith("/pets")) {
                String cpf = path.substring("/tutor/".length(), path.length() - "/pets".length());
                json(resp, 200, Map.of("sucesso", true, "pets", query("SELECT p.codigo_pet AS id, p.codigo_pet AS codigoPet, p.nome, p.especie, p.raca, p.idade, p.porte, p.peso, p.n_documento FROM pet p JOIN tutela t ON t.codigo_pet=p.codigo_pet WHERE t.cpf=? ORDER BY p.nome", cpf)));
                return;
            }
            if ("/login".equals(path) || "/".equals(path)) { req.getRequestDispatcher("/WEB-INF/login.jsp").forward(req, resp); return; }
            if ("/logout".equals(path)) { if (req.getSession(false) != null) req.getSession(false).invalidate(); resp.sendRedirect(req.getContextPath()+"/login"); return; }
            if ("/clientes".equals(path)) { if (!admin(req, resp)) return; req.setAttribute("clientes", query("SELECT cpf, nome, usuario, telefone, email, endereco FROM tutor ORDER BY nome")); req.getRequestDispatcher("/WEB-INF/clientes.jsp").forward(req, resp); return; }
            if ("/pets".equals(path)) { if (!admin(req, resp)) return; req.setAttribute("pets", query("SELECT p.codigo_pet AS id, p.nome, p.especie, p.raca, p.idade, t.cpf AS cpfCliente, t.nome AS nomeCliente FROM pet p LEFT JOIN tutela tu ON tu.codigo_pet=p.codigo_pet LEFT JOIN tutor t ON t.cpf=tu.cpf ORDER BY p.nome")); req.setAttribute("clientes", query("SELECT cpf,nome FROM tutor ORDER BY nome")); req.getRequestDispatcher("/WEB-INF/pets.jsp").forward(req, resp); return; }
            if ("/loja".equals(path)) { if (!admin(req, resp)) return; req.setAttribute("produtos", query("SELECT id_produto AS codigo,nome,marca,tipo AS categoria,estoque,preco,url FROM produto ORDER BY nome")); req.getRequestDispatcher("/WEB-INF/loja.jsp").forward(req, resp); return; }
            if ("/avaliacoes".equals(path)) { if (!admin(req, resp)) return; req.setAttribute("avaliacoes", query("SELECT a.id_avaliacao AS id, a.nota, a.comentario, a.id_atendimentoFK AS atendimento, p.nome AS nomePet, s.nome AS servico FROM avaliacao a LEFT JOIN fila_atendimento f ON f.id_atendimento=a.id_atendimentoFK LEFT JOIN pet p ON p.codigo_pet=f.id_petFK LEFT JOIN servico s ON s.codigo_servico=f.id_servicoFK ORDER BY a.id_avaliacao DESC")); req.getRequestDispatcher("/WEB-INF/avaliacoes.jsp").forward(req, resp); return; }
            if ("/fila".equals(path) || "/FilaServlet".equals(path)) { if (!admin(req, resp)) return; req.setAttribute("fila", query("SELECT f.id_atendimento AS id, f.criticidade AS servico, f.status, f.data_hora_entrada AS horarioChegada, p.nome AS nomePet, t.nome AS nomeCliente, (SELECT COUNT(*) FROM fila_atendimento anterior WHERE anterior.status='Em espera' AND anterior.id_atendimento<=f.id_atendimento) AS posicao, TIMESTAMPDIFF(MINUTE,f.data_hora_entrada,CURRENT_TIMESTAMP) AS esperaMinutos, CASE WHEN f.status='Em atendimento' THEN 'badge-atendimento' WHEN f.status='Concluído' THEN 'badge-ativo' ELSE 'badge-espera' END AS statusClass FROM fila_atendimento f LEFT JOIN pet p ON p.codigo_pet=f.id_petFK LEFT JOIN tutela tu ON tu.codigo_pet=p.codigo_pet LEFT JOIN tutor t ON t.cpf=tu.cpf ORDER BY f.data_hora_entrada")); req.setAttribute("pets",query("SELECT codigo_pet AS id,nome FROM pet ORDER BY nome")); req.setAttribute("servicos",query("SELECT codigo_servico AS id,nome FROM servico ORDER BY nome")); req.getRequestDispatcher("/WEB-INF/fila.jsp").forward(req, resp); return; }
            json(resp, 404, Map.of("sucesso", false, "mensagem", "Rota não encontrada."));
        } catch (SQLException e) { log("Falha na consulta ao MySQL", e); json(resp, 503, Map.of("sucesso", false, "mensagem", "Falha ao consultar o MySQL.")); }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo(); if (path == null || path.isEmpty()) path = req.getServletPath(); if (path == null || path.isEmpty()) path = "/";
        try {
            if ("/usuario".equals(path)) {
                Map<String,Object> b=body(req);String cpf=value(b,"cpf"),nome=value(b,"nome"),usuario=value(b,"usuario"),senha=value(b,"senha");
                if(cpf.isBlank()||nome.isBlank()||usuario.isBlank()||senha.isBlank()){json(resp,400,Map.of("sucesso",false,"mensagem","CPF, nome, usuário e senha são obrigatórios."));return;}
                try(Connection c=MySql.connect();PreparedStatement ps=c.prepareStatement("INSERT INTO tutor(cpf,nome,usuario,senha,telefone,email,endereco,status_conta) VALUES(?,?,?,?,?,?,?,'Ativo')")){
                    ps.setString(1,cpf);ps.setString(2,nome);ps.setString(3,usuario);ps.setString(4,senha);ps.setString(5,value(b,"telefone"));ps.setString(6,value(b,"email"));ps.setString(7,value(b,"endereco"));ps.executeUpdate();
                }
                json(resp,201,Map.of("sucesso",true,"mensagem","Cliente cadastrado."));return;
            }
            if ("/login".equals(path)) {
                String user=req.getParameter("usuario"), pass=req.getParameter("senha");
                String adminUser=setting("ADMIN_USER", "adm");
                String adminPass=System.getenv("ADMIN_PASSWORD");
                if (adminPass != null && !adminPass.isBlank() && adminUser.equals(user) && adminPass.equals(pass)) { req.getSession(true).setAttribute("adminLogado", user); resp.sendRedirect(req.getContextPath()+"/fila"); }
                else { req.setAttribute("erro", adminPass == null || adminPass.isBlank() ? "Configure ADMIN_PASSWORD no ambiente do Tomcat para habilitar o acesso." : "Usuário ou senha inválidos."); req.getRequestDispatcher("/WEB-INF/login.jsp").forward(req, resp); }
                return;
            }
            if ("/admin/pets/salvar".equals(path)) {
                if (!admin(req, resp)) return;
                String cpf=req.getParameter("cpfCliente"), nome=req.getParameter("nome");
                String especie=req.getParameter("especie"), raca=req.getParameter("raca");
                int idade=Integer.parseInt(req.getParameter("idade"));
                if(cpf==null||cpf.isBlank()||nome==null||nome.isBlank()) { resp.sendError(400,"Tutor e nome do pet são obrigatórios."); return; }
                try(Connection c=MySql.connect()) {
                    c.setAutoCommit(false);
                    try(PreparedStatement ps=c.prepareStatement("INSERT INTO pet (nome,especie,raca,idade) VALUES (?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1,nome.trim()); ps.setString(2,especie); ps.setString(3,raca); ps.setInt(4,idade); ps.executeUpdate();
                        try(ResultSet keys=ps.getGeneratedKeys()) { keys.next(); try(PreparedStatement link=c.prepareStatement("INSERT INTO tutela (codigo_pet,cpf) VALUES (?,?)")) { link.setInt(1,keys.getInt(1)); link.setString(2,cpf); link.executeUpdate(); } }
                    }
                    c.commit();
                }
                resp.sendRedirect(req.getContextPath()+"/pets"); return;
            }
            if (path.equals("/admin/clientes/salvar")) {
                if (!admin(req, resp)) return;
                String cpf=req.getParameter("cpf"), nome=req.getParameter("nome"), usuario=req.getParameter("novo_usuario"), senha=req.getParameter("nova_senha");
                if(usuario==null)usuario=req.getParameter("usuario");if(senha==null)senha=req.getParameter("senha");
                if(cpf==null||nome==null||usuario==null||cpf.isBlank()||nome.isBlank()||usuario.isBlank()){resp.sendError(400,"CPF, nome e usuário são obrigatórios.");return;}
                try(Connection c=MySql.connect();PreparedStatement ps=c.prepareStatement("INSERT INTO tutor(cpf,nome,usuario,senha,telefone,email,endereco,status_conta) VALUES(?,?,?,?,?,?,?,'Ativo') ON DUPLICATE KEY UPDATE nome=VALUES(nome),usuario=VALUES(usuario),senha=IF(VALUES(senha)='',senha,VALUES(senha)),telefone=VALUES(telefone),email=VALUES(email),endereco=VALUES(endereco)")){
                    ps.setString(1,cpf.trim());ps.setString(2,nome.trim());ps.setString(3,usuario.trim());ps.setString(4,senha==null?"":senha);ps.setString(5,req.getParameter("telefone"));ps.setString(6,req.getParameter("email"));ps.setString(7,req.getParameter("endereco"));ps.executeUpdate();
                }
                resp.sendRedirect(req.getContextPath()+"/clientes");return;
            }
            if (path.equals("/admin/clientes/excluir")) {
                if (!admin(req, resp)) return;
                try(Connection c=MySql.connect();PreparedStatement ps=c.prepareStatement("DELETE FROM tutor WHERE cpf=?")){ps.setString(1,req.getParameter("cpf"));ps.executeUpdate();}
                resp.sendRedirect(req.getContextPath()+"/clientes");return;
            }
            if (path.equals("/admin/avaliacoes/salvar")) {
                if (!admin(req, resp)) return;
                int nota=Integer.parseInt(req.getParameter("nota"));if(nota<1||nota>3){resp.sendError(400,"Nota deve ser de 1 a 3.");return;}
                String id=req.getParameter("id"), atendimento=req.getParameter("atendimento"), comentario=req.getParameter("comentario");
                if(id==null||id.isBlank()){
                    try(Connection c=MySql.connect();PreparedStatement ps=c.prepareStatement("INSERT INTO avaliacao(nota,comentario,id_atendimentoFK) VALUES(?,?,?)")){ps.setInt(1,nota);ps.setString(2,comentario);if(atendimento==null||atendimento.isBlank())ps.setNull(3,java.sql.Types.INTEGER);else ps.setInt(3,Integer.parseInt(atendimento));ps.executeUpdate();}
                }else try(Connection c=MySql.connect();PreparedStatement ps=c.prepareStatement("UPDATE avaliacao SET nota=?,comentario=? WHERE id_avaliacao=?")){ps.setInt(1,nota);ps.setString(2,comentario);ps.setInt(3,Integer.parseInt(id));ps.executeUpdate();}
                resp.sendRedirect(req.getContextPath()+"/avaliacoes");return;
            }
            if (path.equals("/admin/avaliacoes/excluir")) {
                if (!admin(req, resp)) return;
                try(Connection c=MySql.connect();PreparedStatement ps=c.prepareStatement("DELETE FROM avaliacao WHERE id_avaliacao=?")){ps.setInt(1,Integer.parseInt(req.getParameter("id")));ps.executeUpdate();}
                resp.sendRedirect(req.getContextPath()+"/avaliacoes");return;
            }
            if (path.equals("/admin/produtos/salvar")) {
                if(!admin(req,resp))return;String id=req.getParameter("id");
                if(id==null||id.isBlank())try(Connection c=MySql.connect();PreparedStatement ps=c.prepareStatement("INSERT INTO produto(nome,marca,tipo,estoque,preco,url) VALUES(?,?,?,?,?,?)")){ps.setString(1,req.getParameter("nome"));ps.setString(2,req.getParameter("marca"));ps.setString(3,req.getParameter("categoria"));ps.setInt(4,Integer.parseInt(req.getParameter("estoque")));ps.setBigDecimal(5,new java.math.BigDecimal(req.getParameter("preco")));ps.setString(6,req.getParameter("url"));ps.executeUpdate();}
                else try(Connection c=MySql.connect();PreparedStatement ps=c.prepareStatement("UPDATE produto SET nome=?,marca=?,tipo=?,estoque=?,preco=?,url=? WHERE id_produto=?")){ps.setString(1,req.getParameter("nome"));ps.setString(2,req.getParameter("marca"));ps.setString(3,req.getParameter("categoria"));ps.setInt(4,Integer.parseInt(req.getParameter("estoque")));ps.setBigDecimal(5,new java.math.BigDecimal(req.getParameter("preco")));ps.setString(6,req.getParameter("url"));ps.setInt(7,Integer.parseInt(id));ps.executeUpdate();}
                resp.sendRedirect(req.getContextPath()+"/loja");return;
            }
            if (path.equals("/admin/produtos/excluir")) {if(!admin(req,resp))return;try(Connection c=MySql.connect();PreparedStatement ps=c.prepareStatement("DELETE FROM produto WHERE id_produto=?")){ps.setInt(1,Integer.parseInt(req.getParameter("id")));ps.executeUpdate();}resp.sendRedirect(req.getContextPath()+"/loja");return;}
            if (path.equals("/admin/fila/salvar")) {if(!admin(req,resp))return;try(Connection c=MySql.connect();PreparedStatement ps=c.prepareStatement("INSERT INTO fila_atendimento(criticidade,data_hora_entrada,status,id_servicoFK,id_petFK) VALUES(?,CURRENT_TIMESTAMP,'Em espera',?,?)")){ps.setString(1,req.getParameter("servicoNome"));ps.setInt(2,Integer.parseInt(req.getParameter("servico")));ps.setInt(3,Integer.parseInt(req.getParameter("pet")));ps.executeUpdate();}resp.sendRedirect(req.getContextPath()+"/fila");return;}
            if (path.equals("/admin/fila/excluir")) {if(!admin(req,resp))return;try(Connection c=MySql.connect();PreparedStatement ps=c.prepareStatement("DELETE FROM fila_atendimento WHERE id_atendimento=?")){ps.setInt(1,Integer.parseInt(req.getParameter("id")));ps.executeUpdate();}resp.sendRedirect(req.getContextPath()+"/fila");return;}
            if ("/login/usuario".equals(path) || "/api/login".equals(path)) {
                Map<String,Object> b=body(req); List<Map<String,Object>> found=query("SELECT cpf,nome,usuario,senha FROM tutor WHERE usuario=?", b.get("usuario"));
                if (!found.isEmpty() && String.valueOf(found.get(0).get("senha")).equals(String.valueOf(b.get("senha")))) { Map<String,Object> u=found.get(0); u.remove("senha"); json(resp,200,Map.of("sucesso",true,"cpf",u.get("cpf"),"nome",u.get("nome"),"usuario",u.get("usuario"))); }
                else json(resp,401,Map.of("sucesso",false,"mensagem","Usuário ou senha incorretos.")); return;
            }
            if ("/api/fila/adicionar".equals(path)) {
                Map<String,Object> b=body(req);
                String cpf=value(b,"cpf"), nomePet=value(b,"pet"), nomeServico=value(b,"servico");
                if(cpf.isBlank() || nomePet.isBlank() || nomeServico.isBlank()) {
                    json(resp,400,Map.of("sucesso",false,"mensagem","Informe CPF, pet e serviço.")); return;
                }
                List<Map<String,Object>> pets=query("SELECT p.codigo_pet FROM pet p JOIN tutela t ON t.codigo_pet=p.codigo_pet WHERE t.cpf=? AND p.nome=? LIMIT 1",cpf,nomePet);
                List<Map<String,Object>> servicos=query("SELECT codigo_servico FROM servico WHERE nome=? LIMIT 1",nomeServico);
                if(pets.isEmpty() || servicos.isEmpty()) { json(resp,404,Map.of("sucesso",false,"mensagem","Pet ou serviço não encontrado no cadastro.")); return; }
                int petId=((Number)pets.get(0).get("codigo_pet")).intValue();
                int servicoId=((Number)servicos.get(0).get("codigo_servico")).intValue();
                try(Connection c=MySql.connect(); PreparedStatement ps=c.prepareStatement("INSERT INTO fila_atendimento (criticidade,data_hora_entrada,status,id_servicoFK,id_petFK) VALUES (?,CURRENT_TIMESTAMP,'Em espera',?,?)",Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1,nomeServico); ps.setInt(2,servicoId); ps.setInt(3,petId); ps.executeUpdate();
                    try(ResultSet keys=ps.getGeneratedKeys()){
                        keys.next(); int id=keys.getInt(1);
                        List<Map<String,Object>> pos=query("SELECT COUNT(*) AS total FROM fila_atendimento WHERE status='Em espera' AND id_atendimento<=?",id);
                        json(resp,201,Map.of("id",id,"sucesso",true,"status","Em espera","posicao",pos.get(0).get("total"),"esperaMinutos",0));
                    }
                } return;
            }
            if(path!=null&&path.matches("/api/fila/\\d+/cancelar")){
                String cpf=req.getParameter("cpf");int id=Integer.parseInt(path.split("/")[3]);
                try(Connection c=MySql.connect();PreparedStatement ps=c.prepareStatement("UPDATE fila_atendimento f JOIN pet p ON p.codigo_pet=f.id_petFK JOIN tutela t ON t.codigo_pet=p.codigo_pet SET f.status='Cancelado' WHERE f.id_atendimento=? AND t.cpf=? AND f.status='Em espera'")){ps.setInt(1,id);ps.setString(2,cpf);if(ps.executeUpdate()==0){json(resp,404,Map.of("sucesso",false,"mensagem","Atendimento não encontrado ou já iniciado."));return;}}
                json(resp,200,Map.of("sucesso",true));return;
            }
            if ("/cadastrar/pet".equals(path)) {
                Map<String,Object>b=body(req);String cpf=value(b,"cpf"),nome=value(b,"nome"),especie=value(b,"especie");int idade=Integer.parseInt(String.valueOf(b.get("idade")));
                if(cpf.isBlank()||nome.isBlank()){json(resp,400,Map.of("sucesso",false,"mensagem","CPF e nome do pet são obrigatórios."));return;}
                try(Connection c=MySql.connect()){c.setAutoCommit(false);try(PreparedStatement p=c.prepareStatement("INSERT INTO pet(nome,especie,idade,raca) VALUES(?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){p.setString(1,nome);p.setString(2,especie);p.setInt(3,idade);p.setString(4,especie);p.executeUpdate();try(ResultSet k=p.getGeneratedKeys()){k.next();try(PreparedStatement t=c.prepareStatement("INSERT INTO tutela(codigo_pet,cpf) VALUES(?,?)")){t.setInt(1,k.getInt(1));t.setString(2,cpf);t.executeUpdate();}}}c.commit();}
                json(resp,201,Map.of("sucesso",true,"mensagem","Pet cadastrado."));return;
            }
            if ("/api/avaliacoes".equals(path)) {
                Map<String,Object> b=body(req); int nota=Integer.parseInt(String.valueOf(b.get("nota")));
                if(nota<1||nota>3){json(resp,400,Map.of("sucesso",false,"mensagem","A nota deve ser 1, 2 ou 3."));return;}
                try(Connection c=MySql.connect();PreparedStatement ps=c.prepareStatement("INSERT INTO avaliacao (nota,comentario,id_atendimentoFK) VALUES (?,?,?)")){ps.setInt(1,nota);ps.setString(2,String.valueOf(b.getOrDefault("comentario",""))); Object id=b.getOrDefault("id_atendimento",b.get("servico_id")); if(id==null)ps.setNull(3,java.sql.Types.INTEGER);else ps.setInt(3,Integer.parseInt(id.toString()));ps.executeUpdate();}
                json(resp,201,Map.of("sucesso",true,"mensagem","Avaliação enviada."));return;
            }
            json(resp,404,Map.of("sucesso",false,"mensagem","Rota não encontrada."));
        } catch (SQLException | NumberFormatException e) { json(resp,400,Map.of("sucesso",false,"mensagem","Dados inválidos ou falha na operação MySQL.")); }
    }

    @Override
    protected void doPut(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        req.setCharacterEncoding("UTF-8");String path=req.getPathInfo();if(path==null||path.isEmpty())path=req.getServletPath();
        if(path!=null&&path.matches("/api/pets/\\d+"))try{Map<String,Object>b=body(req);int id=Integer.parseInt(path.substring(path.lastIndexOf('/')+1));try(Connection c=MySql.connect();PreparedStatement ps=c.prepareStatement("UPDATE pet p JOIN tutela t ON t.codigo_pet=p.codigo_pet SET p.peso=?,p.n_documento=?,p.idade=? WHERE p.codigo_pet=? AND t.cpf=?")){ps.setBigDecimal(1,new java.math.BigDecimal(value(b,"peso")));ps.setString(2,value(b,"n_documento"));ps.setInt(3,Integer.parseInt(String.valueOf(b.get("idade"))));ps.setInt(4,id);ps.setString(5,value(b,"cpf"));if(ps.executeUpdate()==0){json(resp,404,Map.of("sucesso",false,"mensagem","Pet não encontrado para este tutor."));return;}}json(resp,200,Map.of("sucesso",true));}catch(SQLException|NumberFormatException e){json(resp,400,Map.of("sucesso",false,"mensagem","Não foi possível atualizar o pet."));}else json(resp,404,Map.of("sucesso",false));
    }

    @Override
    protected void doDelete(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        String path=req.getPathInfo();if(path==null||path.isEmpty())path=req.getServletPath();if(path!=null&&path.matches("/api/pets/\\d+"))try{int id=Integer.parseInt(path.substring(path.lastIndexOf('/')+1));String cpf=req.getParameter("cpf");try(Connection c=MySql.connect();PreparedStatement ps=c.prepareStatement("DELETE p FROM pet p JOIN tutela t ON t.codigo_pet=p.codigo_pet WHERE p.codigo_pet=? AND t.cpf=?")){ps.setInt(1,id);ps.setString(2,cpf);if(ps.executeUpdate()==0){json(resp,404,Map.of("sucesso",false,"mensagem","Pet não encontrado."));return;}}json(resp,200,Map.of("sucesso",true));}catch(SQLException e){json(resp,400,Map.of("sucesso",false));}else json(resp,404,Map.of("sucesso",false));
    }

    private static List<Map<String,Object>> query(String sql,Object... args) throws SQLException {
        try(Connection c=MySql.connect();PreparedStatement ps=c.prepareStatement(sql)){
            for(int i=0;i<args.length;i++)ps.setObject(i+1,args[i]);
            try(ResultSet rs=ps.executeQuery()){
                List<Map<String,Object>> rows=new ArrayList<>(); int n=rs.getMetaData().getColumnCount();
                while(rs.next()){Map<String,Object> row=new LinkedHashMap<>();for(int i=1;i<=n;i++){Object v=rs.getObject(i);row.put(rs.getMetaData().getColumnLabel(i),v instanceof Timestamp t?t.toLocalDateTime().toString():v);}rows.add(row);}return rows;
            }
        }
    }
    private static Map<String,Object> body(HttpServletRequest req) throws IOException {
        if(req.getContentType()!=null && req.getContentType().toLowerCase().contains("application/json"))
            return JSON.readValue(req.getInputStream(),new TypeReference<>(){});
        Map<String,Object> values=new LinkedHashMap<>();
        req.getParameterMap().forEach((key,items)->{if(items!=null&&items.length>0)values.put(key,items[0]);});
        return values;
    }
    private static String value(Map<String,Object> data,String key){Object value=data.get(key);return value==null?"":value.toString().trim();}
    private static String setting(String name,String fallback){String value=System.getenv(name);return value==null||value.isBlank()?fallback:value;}
    private static void json(HttpServletResponse resp,int status,Object value)throws IOException{resp.setStatus(status);resp.setCharacterEncoding("UTF-8");resp.setContentType("application/json");JSON.writeValue(resp.getOutputStream(),value);}
    private static boolean admin(HttpServletRequest req,HttpServletResponse resp)throws IOException{if(req.getSession(false)==null||req.getSession(false).getAttribute("adminLogado")==null){resp.sendRedirect(req.getContextPath()+"/login");return false;}return true;}
}
