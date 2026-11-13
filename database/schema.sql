CREATE DATABASE IF NOT EXISTS smartpet CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smartpet;

CREATE TABLE IF NOT EXISTS `funcionario` (
  `id_funcionario` int NOT NULL AUTO_INCREMENT,
  `matricula` int NOT NULL,
  `nome` varchar(100) NOT NULL,
  `cargo` varchar(50) DEFAULT NULL,
  `crmv` int DEFAULT NULL,
  `salario` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id_funcionario`),
  UNIQUE KEY `matricula` (`matricula`),
  KEY `crmv` (`crmv`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `tutor` (
  `cpf` varchar(12) NOT NULL,
  `nome` varchar(100) NOT NULL,
  `telefone` varchar(20) DEFAULT NULL,
  `endereco` varchar(150) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `usuario` varchar(50) NOT NULL,
  `senha` varchar(50) NOT NULL,
  `status_conta` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`cpf`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `pet` (
  `codigo_pet` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(100) NOT NULL,
  `especie` varchar(50) DEFAULT NULL,
  `porte` varchar(50) DEFAULT NULL,
  `raca` varchar(50) DEFAULT NULL,
  `idade` int DEFAULT NULL,
  `peso` decimal(5,2) DEFAULT NULL,
  `n_documento` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`codigo_pet`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `servico` (
  `codigo_servico` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(100) NOT NULL,
  `preco` decimal(10,2) NOT NULL,
  `categoria` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`codigo_servico`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `caixa` (
  `id_caixa` int NOT NULL AUTO_INCREMENT,
  `saldo_inicial` decimal(10,2) DEFAULT NULL,
  `saldo_final` decimal(10,2) DEFAULT NULL,
  `horario_abertura` datetime DEFAULT NULL,
  `horario_fechamento` datetime DEFAULT NULL,
  `forma_pagamento` varchar(50) DEFAULT NULL,
  `id_funcionarioFK` int DEFAULT NULL,
  PRIMARY KEY (`id_caixa`),
  KEY `id_funcionarioFK` (`id_funcionarioFK`),
  CONSTRAINT `caixa_ibfk_1` FOREIGN KEY (`id_funcionarioFK`) REFERENCES `funcionario` (`id_funcionario`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `venda` (
  `id_venda` int NOT NULL AUTO_INCREMENT,
  `forma_pagamento` varchar(50) DEFAULT NULL,
  `subtotal` decimal(10,2) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `id_caixaFK` int DEFAULT NULL,
  PRIMARY KEY (`id_venda`),
  KEY `id_caixaFK` (`id_caixaFK`),
  CONSTRAINT `venda_ibfk_1` FOREIGN KEY (`id_caixaFK`) REFERENCES `caixa` (`id_caixa`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `fila_atendimento` (
  `id_atendimento` int NOT NULL AUTO_INCREMENT,
  `criticidade` varchar(20) DEFAULT NULL,
  `data_hora_entrada` datetime DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `metodo_cancelamento` varchar(50) DEFAULT NULL,
  `id_servicoFK` int DEFAULT NULL,
  `id_petFK` int DEFAULT NULL,
  `id_funcionarioFK` int DEFAULT NULL,
  PRIMARY KEY (`id_atendimento`),
  KEY `id_servicoFK` (`id_servicoFK`),
  KEY `id_petFK` (`id_petFK`),
  KEY `id_funcionarioFK` (`id_funcionarioFK`),
  CONSTRAINT `fila_atendimento_ibfk_1` FOREIGN KEY (`id_servicoFK`) REFERENCES `servico` (`codigo_servico`) ON DELETE CASCADE,
  CONSTRAINT `fila_atendimento_ibfk_2` FOREIGN KEY (`id_petFK`) REFERENCES `pet` (`codigo_pet`) ON DELETE CASCADE,
  CONSTRAINT `fila_atendimento_ibfk_3` FOREIGN KEY (`id_funcionarioFK`) REFERENCES `funcionario` (`id_funcionario`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `agenda` (
  `id_agenda` int NOT NULL AUTO_INCREMENT,
  `codigo_servico` int NOT NULL,
  `cpf` varchar(12) NOT NULL,
  `data_agendamento` date NOT NULL,
  `hora_agendamento` time NOT NULL,
  `codigo_pet` int DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id_agenda`),
  KEY `id_servicoFK` (`codigo_servico`),
  KEY `cpf_tutorFK` (`cpf`),
  KEY `id_petFK` (`codigo_pet`),
  CONSTRAINT `agenda_ibfk_1` FOREIGN KEY (`codigo_servico`) REFERENCES `servico` (`codigo_servico`) ON DELETE CASCADE,
  CONSTRAINT `agenda_ibfk_2` FOREIGN KEY (`cpf`) REFERENCES `tutor` (`cpf`) ON DELETE CASCADE,
  CONSTRAINT `agenda_ibfk_3` FOREIGN KEY (`codigo_pet`) REFERENCES `pet` (`codigo_pet`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `avaliacao` (
  `id_avaliacao` int NOT NULL AUTO_INCREMENT,
  `comentario` varchar(255) DEFAULT NULL,
  `nota` int DEFAULT NULL,
  `id_atendimentoFK` int DEFAULT NULL,
  PRIMARY KEY (`id_avaliacao`),
  KEY `id_atendimentoFK` (`id_atendimentoFK`),
  CONSTRAINT `avaliacao_ibfk_1` FOREIGN KEY (`id_atendimentoFK`) REFERENCES `fila_atendimento` (`id_atendimento`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `despesa` (
  `id_despesa` int NOT NULL AUTO_INCREMENT,
  `categoria` varchar(50) DEFAULT NULL,
  `descricao` varchar(255) DEFAULT NULL,
  `valor` decimal(10,2) DEFAULT NULL,
  `data_hora` datetime DEFAULT NULL,
  `id_caixaFK` int DEFAULT NULL,
  PRIMARY KEY (`id_despesa`),
  KEY `id_caixaFK` (`id_caixaFK`),
  CONSTRAINT `despesa_ibfk_1` FOREIGN KEY (`id_caixaFK`) REFERENCES `caixa` (`id_caixa`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `receita` (
  `id_receita` int NOT NULL AUTO_INCREMENT,
  `categoria` varchar(50) DEFAULT NULL,
  `descricao` varchar(255) DEFAULT NULL,
  `valor` decimal(10,2) DEFAULT NULL,
  `data_hora` datetime DEFAULT NULL,
  `id_caixaFK` int DEFAULT NULL,
  PRIMARY KEY (`id_receita`),
  KEY `id_caixaFK` (`id_caixaFK`),
  CONSTRAINT `receita_ibfk_1` FOREIGN KEY (`id_caixaFK`) REFERENCES `caixa` (`id_caixa`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `produto` (
  `id_produto` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(100) NOT NULL,
  `marca` varchar(50) DEFAULT NULL,
  `tipo` varchar(50) DEFAULT NULL,
  `estoque` int DEFAULT NULL,
  `preco` decimal(10,2) NOT NULL,
  `codigo` varchar(20) DEFAULT NULL,
  `url` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id_produto`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `item_venda` (
  `id_item` int NOT NULL AUTO_INCREMENT,
  `id_produtoFK` int DEFAULT NULL,
  `id_vendaFK` int DEFAULT NULL,
  `preco_unitario` decimal(10,2) DEFAULT NULL,
  `subtotal` decimal(10,2) DEFAULT NULL,
  `quantidade` int DEFAULT NULL,
  PRIMARY KEY (`id_item`),
  KEY `id_produtoFK` (`id_produtoFK`),
  KEY `id_vendaFK` (`id_vendaFK`),
  CONSTRAINT `item_venda_ibfk_1` FOREIGN KEY (`id_produtoFK`) REFERENCES `produto` (`id_produto`) ON DELETE CASCADE,
  CONSTRAINT `item_venda_ibfk_2` FOREIGN KEY (`id_vendaFK`) REFERENCES `venda` (`id_venda`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `medicamento` (
  `id_medicamento` int NOT NULL AUTO_INCREMENT,
  `remedio` varchar(100) NOT NULL,
  `preco` decimal(10,2) NOT NULL,
  `tipo` varchar(50) NOT NULL,
  PRIMARY KEY (`id_medicamento`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `tutela` (
  `codigo_pet` int NOT NULL,
  `cpf` varchar(12) NOT NULL,
  PRIMARY KEY (`codigo_pet`,`cpf`),
  KEY `cpf_tutorFK` (`cpf`),
  CONSTRAINT `tutela_ibfk_1` FOREIGN KEY (`codigo_pet`) REFERENCES `pet` (`codigo_pet`) ON DELETE CASCADE,
  CONSTRAINT `tutela_ibfk_2` FOREIGN KEY (`cpf`) REFERENCES `tutor` (`cpf`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `usuario_func` (
  `id_usuario` int NOT NULL AUTO_INCREMENT,
  `email` varchar(100) NOT NULL,
  `senha` varchar(50) NOT NULL,
  `matricula_funcFK` int DEFAULT NULL,
  PRIMARY KEY (`id_usuario`),
  KEY `matricula_funcFK` (`matricula_funcFK`),
  CONSTRAINT `usuario_func_ibfk_1` FOREIGN KEY (`matricula_funcFK`) REFERENCES `funcionario` (`matricula`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

