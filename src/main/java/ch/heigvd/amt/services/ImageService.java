package ch.heigvd.amt.services;

import ch.heigvd.amt.database.UpdateHandler;
import ch.heigvd.amt.database.UpdateResult;
import ch.heigvd.amt.database.UpdateStatus;
import ch.heigvd.amt.models.Image;
import ch.heigvd.amt.utils.ResourceLoader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@ApplicationScoped
public class ImageService {

  private final Jdbi jdbi;
  private final UpdateHandler updateHandler;

  private static final Logger logger = Logger.getLogger(ImageService.class);

  @Inject
  public ImageService(Jdbi jdbi, UpdateHandler updateHandler) {
    this.jdbi = jdbi;
    this.updateHandler = updateHandler;
  }

  /**
   * Get an image
   *
   * @param imageId the id of the image
   * @return the image
   */
  public Optional<Image> getImage(int imageId) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(ResourceLoader.loadResource("sql/image/get.sql"))
                .bind("id", imageId)
                .mapTo(Image.class)
                .findOne());
  }

  /**
   * Add an image to the database
   *
   * @param data the data of the image
   * @return The result of the operation with the generated id set if successful
   */
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
      return updateHandler.handleUpdateError(e);
    }
  }

  /**
   * Modify the image data
   *
   * @param data a byte array of the image data
   * @param id the id of the image
   * @return the status of the operation
   */
  public UpdateResult updateImage(byte[] data, int id) {
    try {
      jdbi.useHandle(
          handle ->
              handle
                  .createUpdate(ResourceLoader.loadResource("sql/image/update.sql"))
                  .bind("data", data)
                  .bind("id", id)
                  .execute());
      return new UpdateResult(UpdateStatus.SUCCESS);
    } catch (UnableToExecuteStatementException e) {
      return updateHandler.handleUpdateError(e);
    }
  }

  /**
   * @param inputPart
   * @param id
   * @return
   */
  public int manageImage(InputPart inputPart, int id) {
    // TODO add image treatment to set size.
    try {
      InputStream inputStream = inputPart.getBody(InputStream.class, null);
      byte[] bytes = IOUtils.toByteArray(inputStream);
      if (bytes.length == 0) {
        return -1;
      }
      var res = updateImage(bytes, id);
      if (res.getStatus() == UpdateStatus.SUCCESS) {
        return id;
      }
      return -1;
    } catch (IOException e) {
      e.printStackTrace();
      return -1;
    }
  }

  public int manageImage(InputPart inputPart) {
    try {
      InputStream inputStream = inputPart.getBody(InputStream.class, null);
      byte[] bytes = IOUtils.toByteArray(inputStream);
      if (bytes.length == 0) {
        return Image.DEFAULT_IMAGE_ID;
      }
      var res = addImage(bytes);
      if (res.getStatus() == UpdateStatus.SUCCESS) {
        return res.getGeneratedId();
      }
      return -1;
    } catch (IOException e) {
      e.printStackTrace();
      return -1;
    }
  }
}
