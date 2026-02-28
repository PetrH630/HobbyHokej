package cz.phsoft.hokej.registration.exceptions;
import cz.phsoft.hokej.shared.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

public class PositionCapacityExceededException extends BusinessException {
  public PositionCapacityExceededException(String message) {
          super(message, HttpStatus.CONFLICT);
  }
}
