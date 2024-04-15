package kr.giljabi.api.filter;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

public class CachingPostHttpServletRequest extends HttpServletRequestWrapper {
    private ByteArrayOutputStream cachedBodyOutputStream;
    private ServletInputStream inputStream;

    public CachingPostHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        cacheRequestBody(request);
    }

    private void cacheRequestBody(HttpServletRequest request) throws IOException {
        cachedBodyOutputStream = new ByteArrayOutputStream();
        copy(request.getInputStream(), cachedBodyOutputStream);
        inputStream = new CachedServletInputStream(cachedBodyOutputStream.toByteArray());
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return inputStream;
    }

    private void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    private static class CachedServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream buffer;

        public CachedServletInputStream(byte[] contents) {
            this.buffer = new ByteArrayInputStream(contents);
        }

        @Override
        public int read() throws IOException {
            return buffer.read();
        }

        @Override
        public boolean isFinished() {
            return buffer.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new RuntimeException("Not implemented");
        }
    }
}
