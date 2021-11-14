package ch.heigvd.amt.database;

import java.util.Objects;

public class UpdateResult {

  private final static UpdateResult SUCCESS = new UpdateResult(UpdateStatus.SUCCESS);

  private final UpdateStatus status;
  private final Integer generatedId;

  public UpdateResult(UpdateStatus status, Integer generatedId) {
    this.status = status;
    this.generatedId = generatedId;
  }

  public UpdateResult(UpdateStatus status) {
    this(status, null);
  }

  public UpdateStatus getStatus() {
    return status;
  }

  public Integer getGeneratedId() {
    return generatedId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    UpdateResult that = (UpdateResult) o;

    if (status != that.status) {
      return false;
    }
    return Objects.equals(generatedId, that.generatedId);
  }

  @Override
  public int hashCode() {
    int result = status != null ? status.hashCode() : 0;
    result = 31 * result + (generatedId != null ? generatedId.hashCode() : 0);
    return result;
  }

  public static UpdateResult success(){
    return SUCCESS;
  }
}
