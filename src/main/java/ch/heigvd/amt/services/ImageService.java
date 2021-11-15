package ch.heigvd.amt.services;

import ch.heigvd.amt.database.UpdateResult;
import ch.heigvd.amt.database.UpdateResultHandler;
import ch.heigvd.amt.database.UpdateStatus;
import ch.heigvd.amt.models.Image;
import ch.heigvd.amt.utils.ResourceLoader;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jboss.logging.Logger;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@ApplicationScoped
public class ImageService {

  private final Jdbi jdbi;
  private final UpdateResultHandler updateResultHandler;

  private static final Logger logger = Logger.getLogger(ImageService.class);

  @Inject
  public ImageService(Jdbi jdbi, UpdateResultHandler updateResultHandler) {
    this.jdbi = jdbi;
    this.updateResultHandler = updateResultHandler;
  }

  public Optional<Image> getImage(int imageId) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(ResourceLoader.loadResource("sql/image/get.sql"))
                .bind("id", imageId)
                .mapTo(Image.class)
                .findOne());
  }

  public UpdateResult addImage(byte[] data) {
    try {
      Integer newId =
          jdbi.withHandle(
              handle ->
                  handle
                      .createUpdate(ResourceLoader.loadResource("sql/image/add.sql"))
                      .bind("data", data)
                      .executeAndReturnGeneratedKeys()
                      .mapTo(int.class)
                      .one());
      return new UpdateResult(UpdateStatus.SUCCESS, newId);
    } catch (UnableToExecuteStatementException e) {
      return updateResultHandler.handleUpdateError(e);
    }
  }
}
