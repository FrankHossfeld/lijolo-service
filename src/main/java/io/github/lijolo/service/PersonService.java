package io.github.lijolo.service;

import io.github.lijolo.model.dto.Anschrift;
import io.github.lijolo.model.dto.Person;
import io.github.lijolo.model.sql.tables.AnschriftJooq;
import io.github.lijolo.model.sql.tables.PersonJooq;
import io.github.lijolo.model.sql.tables.records.AnschriftRecord;
import io.github.lijolo.model.sql.tables.records.PersonRecord;
import io.github.lijolo.service.exception.DataNotFoundException;
import org.jooq.Condition;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PersonService
    extends AbstractService {

  public PersonService() {
    super();
  }

  public Person get(Connection con,
                    Integer key)
      throws SQLException {
    Optional<PersonRecord> optional = super.getDslContext(con)
                                           .selectFrom(PersonJooq.PERSON)
                                           .where(PersonJooq.PERSON.PERSON_NR.eq(key))
                                           .fetchOptional();
    if (optional.isPresent()) {
      Person model = optional.get()
                             .into(Person.class);
      model.setAnschriften(this.getAnschriftenFor(con,
                                                  key));
      con.close();
      return model;
    }
    throw new DataNotFoundException("Person with personNr >>" + key + "<< not found");
  }

  private List<Anschrift> getAnschriftenFor(Connection con,
                                            Integer key) {
    List<Condition> conditionList = new ArrayList<>();
    conditionList.add(AnschriftJooq.ANSCHRIFT.PERSON_NR.eq(key));
    return super.getDslContext(con)
                .selectFrom(AnschriftJooq.ANSCHRIFT)
                .where(conditionList)
                .fetch()
                .into(Anschrift.class);
  }

  public Person insert(Connection con,
                       Person model)
      throws SQLException {
    PersonRecord newRecord = super.getDslContext(con)
                                  .newRecord(PersonJooq.PERSON,
                                             model);
    newRecord.insert();
    newRecord.refresh();
    Person newModel = newRecord.into(Person.class);
    con.commit();
    con.close();
    return newModel;
  }

  private Anschrift insertAnschrift(Connection con,
                                    Integer personNr,
                                    Anschrift anschrift) {
    anschrift.setPersonNr(personNr);
    AnschriftRecord newRecord = super.getDslContext(con)
                                     .newRecord(AnschriftJooq.ANSCHRIFT,
                                                anschrift);
    newRecord.insert();
    newRecord.refresh();
    return newRecord.into(Anschrift.class);
  }

}
