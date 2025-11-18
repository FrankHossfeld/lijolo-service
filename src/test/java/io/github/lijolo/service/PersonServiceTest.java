package io.github.lijolo.service;

import io.github.lijolo.model.dto.Anschrift;
import io.github.lijolo.model.dto.Person;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PersonServiceTest
    extends AbstractServiceTest {

  private PersonService service;

  @BeforeAll
  public void beforeAll() {
  }

  @BeforeEach
  public void beforeEach() {
    this.service = new PersonService();
    this.setUpDataBaseConfiguration();
  }

  @Test
  void insertAndGet01()
      throws SQLException {
    Person model = new Person();
    model.setName01("test");
    model.setKunde(true);
    model.getAnschriften()
         .add(new Anschrift("Test Strasse",
                            "21a",
                            "47110",
                            "Kölsches Wasser"));
    Person newModel = this.service.insert(super.getConnection(),
                                          model);
    assertNotNull(newModel);
    Person readModel = this.service.get(super.getConnection(),
                                        newModel.getPersonNr());
    assertNotNull(readModel);
    assertEquals(newModel.getName01(),
                 readModel.getName01());
    assertEquals(1,
                 readModel.getAnschriften()
                          .size());
    readModel.getAnschriften()
             .forEach(a -> assertNotNull(a.getPersonNr()));
  }

  @Test
  void insertAndGet02()
      throws SQLException {
    Person model = new Person();
    model.setName01("test");
    model.setKunde(true);
    model.getAnschriften()
         .add(new Anschrift("Test Strasse 01",
                            "21a",
                            "47110",
                            "Kölsches Wasser 01"));
    model.getAnschriften()
         .add(new Anschrift("Test Strasse 02",
                            "21a",
                            "47110",
                            "Kölsches Wasser 02"));
    Person newModel = this.service.insert(super.getConnection(),
                                          model);
    assertNotNull(newModel);
    Person readModel = this.service.get(super.getConnection(),
                                        newModel.getPersonNr());
    assertNotNull(readModel);
    assertEquals(newModel.getName01(),
                 readModel.getName01());
    assertEquals(2,
                 readModel.getAnschriften()
                          .size());
    readModel.getAnschriften()
             .forEach(a -> assertNotNull(a.getPersonNr()));
  }

}