package se.kodapan.service.template.prevalence;

/**
 * Created by kalle on 2017-03-22.
 */
public class TestTransaction implements Transaction<TestMessageQueuePrevalence.Root, TestMessageQueuePrevalence.Payload, TestMessageQueuePrevalence.Response> {
  @Override
  public String getStereotype() {
    return "TestTransaction";
  }

  @Override
  public Integer getVersion() {
    return 1;
  }

  @Override
  public Class<TestMessageQueuePrevalence.Payload> getPayloadClass() {
    return TestMessageQueuePrevalence.Payload.class;
  }

  @Override
  public Class<TestMessageQueuePrevalence.Response> getResponseClass() {
    return TestMessageQueuePrevalence.Response.class;
  }

  @Override
  public TestMessageQueuePrevalence.Response execute(TestMessageQueuePrevalence.Root root, TestMessageQueuePrevalence.Payload payload) throws Exception {
    TestMessageQueuePrevalence.Response response = new TestMessageQueuePrevalence.Response();
    response.setSum(root.getCounter().addAndGet(payload.getIncrement()));
    return response;
  }
}
