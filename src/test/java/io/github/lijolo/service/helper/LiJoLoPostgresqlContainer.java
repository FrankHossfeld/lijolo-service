package io.github.lijolo.service.helper;

import org.testcontainers.containers.PostgreSQLContainer;

public class LiJoLoPostgresqlContainer
    extends PostgreSQLContainer<LiJoLoPostgresqlContainer> {

  private static final String IMAGE_VERSION = "postgres:15";

  public LiJoLoPostgresqlContainer() {
    super(LiJoLoPostgresqlContainer.IMAGE_VERSION);
  }
}
