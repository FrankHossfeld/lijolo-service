package io.github.lijolo.service;

import io.github.lijolo.model.jooq.DslContextProvider;
import org.jooq.DSLContext;

import java.sql.Connection;

public class AbstractService {

  private String password;
  private String user;
  private String jdbcUrl;

  public AbstractService() {
  }

  protected DSLContext getDslContext(Connection con) {
    return DslContextProvider.INSTANCE.getDslContext(con);
  }

}
