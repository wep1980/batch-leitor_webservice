package br.com.wepdev.servicereaderjob.reader;

import br.com.wepdev.servicereaderjob.domain.ResponseUser;
import br.com.wepdev.servicereaderjob.domain.User;
import org.springframework.batch.core.annotation.AfterChunk;
import org.springframework.batch.core.annotation.BeforeChunk;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserReader implements ItemReader<User> {


  private RestTemplate restTemplate = new RestTemplate();
  private int page = 1;
  private List<User> users = new ArrayList<>(); // Lista que guarda os usuarios
  private int userIndex = 0; // Indice do usuario que avisa qual usuario esta sendo lido
  private int total = 0; // variavel criada para op controlar o total de itens lidos e fazer o controle do linmite configurado no application.properties

  @Value("${chunkSize}")
  private int chunkSize; // Variavel que pega o valor da propriedade configurada no application.properties

  @Value("${chunkSize}")
  private int pageSize;  // Variavel que pega o valor da propriedade configurada no application.properties

  @Value("${limit}") //Para limitar a leitura e o batch nao ficar muito longo.
  private int limit;  // Variavel que pega o valor da propriedade configurada no application.properties


  /**
   * Esse metodo read() devolve apenas um Usuario por vez, esse metodo deve parar de executar quando nao houver mais usuarios na lista
   */
  @Override
  public User read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

    User user;
    // Logica de leitura da Lista de usuarios
    if(userIndex < users.size())
       user = users.get(userIndex); // Se o indice for valido, e retornado o indice atual
    else // Se ja acabou os registros de usuarios, o user recebe null, pois nao existem mais usuarios na lista. Assim acaba a leitura
       user = null;

       userIndex++;
       return user;
  }

  /**
   * Metodo que faz a chamada HTTP da Api, contem os dados do usuario a partir da api de forma paginada
   * @return
   */
  private List<User> fetchUserDataFromAPI(){
    ResponseEntity<ResponseUser> response = restTemplate
            .getForEntity(String.format("https://gorest.co.in/public/v1/users?page=%d", page), ResponseUser.class);
    List<User> userData = response.getBody().getData();
    return userData;
  }


  /**
   * Metodo que dispara o Evento para o carregamento de uma pagina do webservice.
   * Evento que ocorre antes de carregar o chunk, ou seja antes de cada chunk for lido e feita a chamada do metodo fetchUserDataFromAPI().
   * A Pagina sera carregada ate o limite do chunk size que esta confogurado no application.properties.
   * Para limitar a leitura e o batch nao ficar muito longo foi configurado um limit=40 no application.properties.
   * O carregamento das paginas e feito antes de cada chunk, e depois de cada chunk e preciso resetar os contadores para leitura continuar sendo feita
   */
  @BeforeChunk
  private void beforeChunk(ChunkContext context){
      for(int i = 0; i < chunkSize; i += pageSize){
        if(total >= limit) return; // se o total de itens ja lidos for maior qye o limit e feito o retorno nao e mais feito o carregamento de usuarios
          users.addAll(fetchUserDataFromAPI());
          total += pageSize; // para somar o total de itens lidos
          page ++; // vai incrementando a pagina que esta sendo lida atualmente
    }
  }

  /**
   * metodo(evento) executado apos o chunk para resetar os contadores
   */
  @AfterChunk
  private void afterChunk(ChunkContext context){
    System.out.println("********** FIM DO CHUNK **********");
    userIndex =0;
    users = new ArrayList<>();
  }

}
