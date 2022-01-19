package ch.heigvd.amt.services;

import ch.heigvd.amt.database.UpdateHandler;
import ch.heigvd.amt.utils.CookieClientRequestFilter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataWriter;
import org.jdbi.v3.core.Jdbi;

@ApplicationScoped
public class ImageService {

  public static final int IMAGE_WIDTH = 250;
  public static final int IMAGE_HEIGTH = 300;
  private final Jdbi jdbi;
  private final UpdateHandler updateHandler;

  @ConfigProperty(name = "cdn.server.url")
  String CDN_ADDR;

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
  public byte[] getImage(UUID imageId, String token) {
    return get(String.valueOf(imageId), token);
  }

  public byte[] getDefaultImage(String token) {
    return get("/default", token);
  }

  private byte[] get(String data, String token) {
    Response r = null;
    try {
      Client client = ClientBuilder.newClient();
      r = client.target(CDN_ADDR + "/image").path(data).request().get();
      if (r.getStatus() != Status.OK.getStatusCode()) {
        if (r.getStatus() == Status.NOT_FOUND.getStatusCode()) {
          throw new NotFoundException();
        }
        throw new ImageException();
      }
      return r.readEntity(byte[].class);
    } finally {
      if (r != null) {
        r.close();
      }
    }
  }

  /**
   * Add an image to the database
   *
   * @param data the data of the image
   * @return The generated id set
   */
  private UUID addImageToDB(byte[] data, String token, String query) {
    Response r = null;
    try {
      MultipartFormDataOutput output = new MultipartFormDataOutput();
      output.addFormData("image", data, MediaType.APPLICATION_OCTET_STREAM_TYPE);
      Client client = ClientBuilder.newClient();
      r =
          client
              .target(CDN_ADDR + "/image?default=" + query)
              .register(MultipartFormDataWriter.class)
              .register(new CookieClientRequestFilter(token))
              .request()
              .post(Entity.entity(output, MediaType.MULTIPART_FORM_DATA));

      if (r.getStatus() != Status.CREATED.getStatusCode()) {
        logger.warn(r.getStatus());
        throw new ImageException();
      }
      return r.readEntity(UUID.class);
    } finally {
      if (r != null) {
        r.close();
      }
    }
  }

  /**
   * @param imageData an array with the image data
   * @return the id of the new image
   * @throws IOException if an IO Exception occurs
   */
  public UUID addImage(byte[] imageData, String token) throws IOException {
    return addImageToDB(rescaleImage(imageData), token, "false");
  }

  /**
   * @param imageData an array with the image data
   * @return the id of the new image
   * @throws IOException if an IO Exception occurs
   */
  public UUID addDefaultImage(byte[] imageData, String token) throws IOException {
    return addImageToDB(rescaleImage(imageData), token, "true");
  }

  /**
   * @param input the data of the image
   * @return the data of the image rescaled
   * @throws IOException if an IO Exception occurs
   */
  private byte[] rescaleImage(byte[] input) throws IOException {
    java.awt.Image image =
        ImageIO.read(new ByteArrayInputStream(input))
            .getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGTH, java.awt.Image.SCALE_DEFAULT);
    BufferedImage rescaledImage =
        new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGTH, BufferedImage.TYPE_INT_RGB);
    rescaledImage.getGraphics().drawImage(image, 0, 0, null);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ImageIO.write(rescaledImage, "png", output);
    return output.toByteArray();
  }
}
