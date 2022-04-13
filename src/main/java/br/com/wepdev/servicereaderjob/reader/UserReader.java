package br.com.wepdev.servicereaderjob.reader;

import br.com.wepdev.servicereaderjob.domain.ResponseUser;
import br.com.wepdev.servicereaderjob.domain.User;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
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


  /**
   * Esse metodo read() devolve apenas um Usuario por vez, esse metodo deve para de executar quando nao houver mais usuarios na lista
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

}
