INSERT INTO servico (id, nome, descricao, preco, duracao) VALUES
(1, 'Unhas em Gel (Tips)', 'Alongamento com tips para um acabamento duradouro e natural.', 100.00, 120),
(2, 'Unhas de Fibra de Vidro', 'Técnica de alongamento que usa fios de fibra para um resultado resistente e fino.', 140.00, 180),
(3, 'Banho de Gel', 'Aplicação de gel sobre as unhas naturais para fortalecimento e brilho.', 80.00, 90),
(4, 'Blindagem de Unhas', 'Cria uma camada protetora sobre as unhas naturais para evitar quebras e lascas.', 50.00, 60),
(5, 'Manutenção de Unha de Gel', 'Manutenção do alongamento em gel para garantir a durabilidade e aparência.', 80.00, 100),
(6, 'Manutenção de Banho de Gel', 'Manutenção da camada de gel sobre as unhas naturais.', 60.00, 75),
(7, 'Francesa Reversa (Adicional)', 'Decoração avançada para um efeito de francesinha estrutural e marcante.', 10.00, 30),
(8, 'Decoração 3D (Adicional por unha)', 'Aplicação de elementos decorativos em relevo para um design único.', 15.00, 20),
(9, 'Esmaltação em Gel', 'Esmaltação de longa duração com secagem imediata e brilho intenso.', 50.00, 45),
(10, 'Mão (Cutilagem e Esmaltação)', 'Cuidado tradicional com cutilagem e esmaltação para as mãos.', 25.00, 40),
(11, 'Pé (Cutilagem e Esmaltação)', 'Cuidado tradicional com cutilagem e esmaltação para os pés.', 30.00, 50),
(12, 'Mão e Pé', 'Pacote completo de cuidado tradicional para mãos e pés.', 50.00, 90);

INSERT INTO servico_imagens (servico_id, imagens) VALUES
(1, 'img/close-up-manicurist-using-nail-polish.jpg'),
(2, '/img/unha-2.jpg'),
(3, '/img/unha-3.jpg'),
(4, '/img/unha-4.jpg'),
(5, '/img/unha-5.jpg'),
(6, '/img/unha-5.jpg'),
(7, '/img/unha-6.jpg'),
(8, '/img/unha-6.jpg'),
(9, '/img/unha-6.jpg'),
(10, '/img/unha-6.jpg'),
(11, '/img/unha-6.jpg'),
(12, '/img/unha-6.jpg');

SELECT setval(pg_get_serial_sequence('servico', 'id'), (SELECT MAX(id) FROM servico));
