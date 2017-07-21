package se.kodapan.service.template.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import org.junit.Assert;
import org.junit.Test;
import se.kodapan.service.template.ServiceModule;

import java.time.OffsetDateTime;
import java.util.UUID;

public class TestMessageQueueMessage {

  @Test
  public void test() throws Exception {

    String json = "" +
        "{" +
        "\"identity\": \""+ UUID.randomUUID().toString()+"\"," +
        "\"created\": \""+ OffsetDateTime.now().toString()+"\"," +
        "\"stereotype\": \"stereotype\"," +
        "\"version\": 1," +
        "\"payload\": {"+
        "  \"payloadidentity\": \""+ UUID.randomUUID().toString()+"\"," +
        "  \"payloadcreated\": \""+ OffsetDateTime.now().toString()+"\"," +
        "  \"payloadstereotype\": \"stereotype\"" +
        "}" +
        "}";

    ObjectMapper objectMapper = Guice.createInjector(new ServiceModule("test")).getInstance(ObjectMapper.class);

    MessageQueueMessage message1 = objectMapper.readValue(json, MessageQueueMessage.class);
    MessageQueueMessage message2 = objectMapper.readValue(json, MessageQueueMessage.class);

    Assert.assertEquals(message1, message2);


  }

}
