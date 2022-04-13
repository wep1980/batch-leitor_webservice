package br.com.wepdev.servicereaderjob.domain;

import java.util.List;

/**
 * Classe que mapeia e encapsula os dados de usuarios vindos da Api atraves do cliente RestTemplate
 */
public class ResponseUser {


  private List<User> data;

  public ResponseUser() {
  }

  public List<User> getData() {
    return data;
  }

  public void setData(List<User> data) {
    this.data = data;
  }

}
