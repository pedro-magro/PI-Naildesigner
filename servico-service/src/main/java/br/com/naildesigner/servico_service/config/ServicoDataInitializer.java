package br.com.naildesigner.servico_service.config;

import br.com.naildesigner.servico_service.models.Servico;
import br.com.naildesigner.servico_service.repositories.ServicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ServicoDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ServicoDataInitializer.class);

    @Autowired
    private ServicoRepository servicoRepository;

    @Override
    public void run(String... args) throws Exception {
        if (servicoRepository.count() == 0) {
            logger.info("Banco de dados de serviços vazio. A popular com dados iniciais e imagens corretas...");

            List<Servico> servicosParaSalvar = new ArrayList<>();

            // Cada serviço agora tem o seu caminho de imagem correto, como no HTML original.
            
            Servico s1 = new Servico();
            s1.setNome("Unhas em Gel (Tips)");
            s1.setDescricao("Alongamento com tips para um acabamento duradouro e natural.");
            s1.setPreco(100.00);
            s1.setDuracao(120);
            s1.setImagens(Arrays.asList("img/close-up-manicurist-using-nail-polish.jpg"));
            servicosParaSalvar.add(s1);

            Servico s2 = new Servico();
            s2.setNome("Unhas de Fibra de Vidro");
            s2.setDescricao("Técnica de alongamento que usa fios de fibra para um resultado resistente e fino.");
            s2.setPreco(140.00);
            s2.setDuracao(180);
            s2.setImagens(Arrays.asList("/img/unha-2.jpg"));
            servicosParaSalvar.add(s2);
            
            Servico s3 = new Servico();
            s3.setNome("Banho de Gel");
            s3.setDescricao("Aplicação de gel sobre as unhas naturais para fortalecimento e brilho.");
            s3.setPreco(80.00);
            s3.setDuracao(90);
            s3.setImagens(Arrays.asList("/img/unha-3.jpg"));
            servicosParaSalvar.add(s3);
            
            Servico s4 = new Servico();
            s4.setNome("Blindagem de Unhas");
            s4.setDescricao("Cria uma camada protetora sobre as unhas naturais para evitar quebras e lascas.");
            s4.setPreco(50.00);
            s4.setDuracao(60);
            s4.setImagens(Arrays.asList("/img/unha-4.jpg"));
            servicosParaSalvar.add(s4);

            Servico s5 = new Servico();
            s5.setNome("Manutenção de Unha de Gel");
            s5.setDescricao("Manutenção do alongamento em gel para garantir a durabilidade e aparência.");
            s5.setPreco(80.00);
            s5.setDuracao(100);
            s5.setImagens(Arrays.asList("/img/unha-5.jpg"));
            servicosParaSalvar.add(s5);

            Servico s6 = new Servico();
            s6.setNome("Manutenção de Banho de Gel");
            s6.setDescricao("Manutenção da camada de gel sobre as unhas naturais.");
            s6.setPreco(60.00);
            s6.setDuracao(75);
            s6.setImagens(Arrays.asList("/img/unha-5.jpg")); // Imagem repetida, como no original
            servicosParaSalvar.add(s6);
            
            Servico s7 = new Servico();
            s7.setNome("Francesa Reversa (Adicional)");
            s7.setDescricao("Decoração avançada para um efeito de francesinha estrutural e marcante.");
            s7.setPreco(10.00);
            s7.setDuracao(30);
            s7.setImagens(Arrays.asList("/img/unha-6.jpg"));
            servicosParaSalvar.add(s7);

            Servico s8 = new Servico();
            s8.setNome("Decoração 3D (Adicional por unha)");
            s8.setDescricao("Aplicação de elementos decorativos em relevo para um design único.");
            s8.setPreco(15.00);
            s8.setDuracao(20);
            s8.setImagens(Arrays.asList("/img/unha-6.jpg")); // Imagem repetida
            servicosParaSalvar.add(s8);
            
            Servico s9 = new Servico();
            s9.setNome("Esmaltação em Gel");
            s9.setDescricao("Esmaltação de longa duração com secagem imediata e brilho intenso.");
            s9.setPreco(50.00);
            s9.setDuracao(45);
            s9.setImagens(Arrays.asList("/img/unha-6.jpg")); // Imagem repetida
            servicosParaSalvar.add(s9);

            Servico s10 = new Servico();
            s10.setNome("Mão (Cutilagem e Esmaltação)");
            s10.setDescricao("Cuidado tradicional com cutilagem e esmaltação para as mãos.");
            s10.setPreco(25.00);
            s10.setDuracao(40);
            s10.setImagens(Arrays.asList("/img/unha-6.jpg")); // Imagem repetida
            servicosParaSalvar.add(s10);

            Servico s11 = new Servico();
            s11.setNome("Pé (Cutilagem e Esmaltação)");
            s11.setDescricao("Cuidado tradicional com cutilagem e esmaltação para os pés.");
            s11.setPreco(30.00);
            s11.setDuracao(50);
            s11.setImagens(Arrays.asList("/img/unha-6.jpg")); // Imagem repetida
            servicosParaSalvar.add(s11);
            
            Servico s12 = new Servico();
            s12.setNome("Mão e Pé");
            s12.setDescricao("Pacote completo de cuidado tradicional para mãos e pés.");
            s12.setPreco(50.00);
            s12.setDuracao(90);
            s12.setImagens(Arrays.asList("/img/unha-6.jpg")); // Imagem repetida
            servicosParaSalvar.add(s12);

            servicoRepository.saveAll(servicosParaSalvar);
            logger.info(">> {} serviços foram guardados na base de dados.", servicosParaSalvar.size());
        } else {
            logger.info("A base de dados de serviços já contém dados. Nenhuma ação necessária.");
        }
    }
}