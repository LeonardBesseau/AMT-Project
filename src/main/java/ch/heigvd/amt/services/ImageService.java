package ch.heigvd.amt.services;

import ch.heigvd.amt.database.UpdateResult;
import ch.heigvd.amt.database.UpdateResultHandler;
import ch.heigvd.amt.database.UpdateStatus;
import ch.heigvd.amt.models.Image;
import ch.heigvd.amt.utils.ResourceLoader;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import org.jboss.logging.Logger;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@ApplicationScoped
public class ImageService {

  public static final int IMAGE_WIDTH = 250;
  public static final int IMAGE_HEIGTH = 300;
  private final Jdbi jdbi;
  private final UpdateResultHandler updateResultHandler;

  private static final Logger logger = Logger.getLogger(ImageService.class);

  @Inject
  public ImageService(Jdbi jdbi, UpdateResultHandler updateResultHandler) {
    this.jdbi = jdbi;
    this.updateResultHandler = updateResultHandler;
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
  private UpdateResult addImageToDB(byte[] data) {
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

  /**
   * Modify the image data
   *
   * @param data a byte array of the image data
   * @param id the id of the image
   * @return the status of the operation
   */
  private UpdateResult updateImageToDB(byte[] data, int id) {
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
      return updateResultHandler.handleUpdateError(e);
    }
  }

  /**
   * @param imageData an array with the image data
   * @param id the id of the image to update
   * @return the result of the operation
   */
  public UpdateResult updateImage(byte[] imageData, int id) throws IOException {
    return updateImageToDB(rescaleImage(imageData), id);
  }

  /**
   * @param imageData an array with the image data
   * @return the result of the operation in the database
   * @throws IOException if an IO Exception occurs
   */
  public UpdateResult addImage(byte[] imageData) throws IOException {
    return addImageToDB(rescaleImage(imageData));
  }

  /**
   * @param input the data of the image
   * @return the data of the image rescaled
   * @throws IOException if an IO Exception occurs
   */
  private byte[] rescaleImage(byte[] input) throws IOException {
    BufferedImage image = ImageIO.read(new ByteArrayInputStream(input));
    BufferedImage rescaledImage =
        new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGTH, BufferedImage.TYPE_INT_RGB);
    rescaledImage.getGraphics().drawImage(image, 0, 0, null);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ImageIO.write(rescaledImage, "png", output);
    return output.toByteArray();
  }
}
