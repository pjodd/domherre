package se.kodapan.service.template.mq.localfs;

import lombok.Data;

import java.io.File;

/**
 * Created by kalle on 2017-03-22.
 */
@Data
public class LocalFsMessageQueue {

  private File rootPath;

}
