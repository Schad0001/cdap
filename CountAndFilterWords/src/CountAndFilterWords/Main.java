package CountAndFilterWords;

import com.continuuity.api.flow.Application;
import com.continuuity.api.flow.ApplicationSpecification;
import com.continuuity.api.stream.Stream;

/**
 * CountAppDemo application that contains multiple flows attached to a stream named "text"
 */
public class Main implements Application {
  @Override
  public ApplicationSpecification configure() {
    return ApplicationSpecification.builder()
      .setApplicationName("CountAppDemo")
      .addFlow(CountAndFilterWords.class)
      .addStream(new Stream("text"))
      .create();
  }
}
