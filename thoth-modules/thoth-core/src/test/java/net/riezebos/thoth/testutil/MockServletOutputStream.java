package net.riezebos.thoth.testutil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

public class MockServletOutputStream extends ServletOutputStream {

  private ByteArrayOutputStream bos = new ByteArrayOutputStream();

  public byte[] getContents() {
    return bos.toByteArray();
  }

  public String getContentsAsString() {
    try {
      return new String(bos.toByteArray(), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void setWriteListener(WriteListener writeListener) {

  }

  @Override
  public void write(int b) throws IOException {
    bos.write(b);
  }

}
