package kr.kro.deom.common.file.exception;

import kr.kro.deom.common.exception.exceptions.CustomException;
import kr.kro.deom.common.response.BaseResponseCode;

public class S3FileUploadException extends CustomException {
    public S3FileUploadException(BaseResponseCode code) {
        super(code);
    }
}
