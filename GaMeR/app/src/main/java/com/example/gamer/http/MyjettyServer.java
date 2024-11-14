package com.example.gamer.http;

import android.os.Environment;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;

import javax.servlet.ServletContext;

public class MyjettyServer {

    private static Server server;
    String fileLink1;

    public static void main(String[] args) throws Exception {
        server = new Server();

        // Create an HTTP/2 connection factory with HTTP/1.1 compatibility
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSendXPoweredBy(false);
        httpConfig.setSendServerVersion(false);
        ServerConnector http2Connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        http2Connector.setPort(8080); // Set the port you want to listen on
        server.addConnector(http2Connector);

        ContextHandler context = new ContextHandler("/");
        context.setHandler(new MyHandler());
        server.setHandler(context);

        server.start();
        System.out.println("Server started on port 8080");
        server.join();
    }

    public static void stop() throws Exception {
        server.stop();
    }

    private static class MyHandler extends AbstractHandler {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            String uri = request.getRequestURI();

            String decodedUri = URLDecoder.decode(uri, "UTF-8");

            // If the request is for a specific file, serve that file for download
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + decodedUri);

            if (file.isDirectory()) {
                File[] files = file.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return !pathname.isHidden(); // Skip hidden files and directories
                    }
                });

                // Sort directories alphabetically
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File file1, File file2) {
                        if (file1.isDirectory() && file2.isDirectory()) {
                            return file1.getName().compareToIgnoreCase(file2.getName());
                        } else if (file1.isDirectory()) {
                            return -1; // Directory comes before file
                        } else if (file2.isDirectory()) {
                            return 1; // File comes after directory
                        } else {
                            return file1.getName().compareToIgnoreCase(file2.getName());
                        }
                    }
                });

                StringBuilder builder = new StringBuilder();
                builder.append("<html><head>");
                builder.append("<style>");
                builder.append("body { font-family: Arial, sans-serif; }");
                builder.append("ul { list-style-type: none; padding: 0; }");
                builder.append(".file-link { display: block; padding: 5px 10px; text-decoration: none; }");
                builder.append(".file-link:hover { background-color: #f5f5f5; }");
                builder.append(".folder-icon { display: inline-block; width: 50px; height: 50px; background-image: url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAYAAAAeP4ixAAAACXBIWXMAAAsTAAALEwEAmpwYAAADrElEQVR4nO2ZWWgTURSGb9vEJlFMVWwTRRE3BBWbxAcF0UfFFYozfVAwqaVqxaWbpVgNIqJYTay5FQrig28uTz4IbmSioiAVsTOpuL0pKGpNZ2zStOgvSV1CzDKxSWYK+eEnZJg553z3zL1zkyGkoILUr/nrbpZaWK/DwnCty6sezCXjUk4UW1junpX14ZclW5V3EVGDzvdiOxWwTc65NoZbGgMRtYXlTqe7ztOHhR4BxztfYXJWik6YRMBHKuCDnHNt1ffmxYNYWc4pI8cJKgBdPDbFHl+zxquxML7dkcGorOIWjIWDdPphoQIq5Z5vYTkaA/K6krk/Pd013T0wUj+2XL2KktjjVtbX9icWw72PzD+ST1mZBzYr61tr29hjGFMclrsW291l1d45ZDzKWn1/o4XhhqNzjeFuEYIiMl61rNo7x8Z4V0Xmi9K1kC4eszw8rlzohS0vCcMu85sht+lLSrtMn8PnTDsyiesRsDmyKnl4NJN8KOQyDQ+5zUjnkNv8I+SuqMkkNvVjcfyqpDjI0H/CZFOURzvlcWnMIErDUAEPPTw+Ob3QjBkkCuOKfg7nxWdN63/X2t0Dg/sZyhJS/g9IXu2auVVWuwog7kJHoNytdc4MPC3Ji8PUlGMQgeTFYVqRYxCe5MXhbIOEzpoQsBP1ubZkBE5SLBskeGyK8kXb/7W4v/RRRh0Z2DNBnSCH9FXyQc6o9LaqKxnKaI4MHilLGbC/VoePDXNz4v5aXfJuHNDdzghErNOkBOm7fAx33yHr5l59Qn/dpMR5HQRi66TVskFCHeUpIb7sMsL75mtOQF5ebE6ad2CPRkwKkQhk8LBRfd2wE3xr0F3LCESsLVZdNwIOAqlt4hL5IKfKVdmNQL32c0qIeJBvh5IH+1qjwdNb1/HEdyfrfnvekXIABxr1F2WDhNxmBGqS31aK2VGEYOuU2fJBTk5Xvmh7gm7snfAuLUQsiNQ0UfGiAwksNek7MgD5uyURd2sh1mtHR6NeC6nJoJwb9D8CDZOnygYJnpj2ByR0uhyhMxXRJS+ylVfyH5Tg8ampH4LxINJB/d9WHtRDahz9LjUaMNhuVMzBduMF+SAdFUGl50EgkXcWf4eT6GSDDB4tkxQv2v6vxX2lz0kmkloMN6T9pY/VZrFZvyW+VurH6i4eiX9YjSdRHn4qYKS7B9qcJIgEpjxaIu9DSA7V2YcVXb3YkLMEnl6sjLydogLS74nULABFVMBWzwvMULqWgojC+gmNpi1k+dHCiQAAAABJRU5ErkJggg=='); }");
                builder.append(".file-icon { display: inline-block; width: 40px; height: 40px; background-image: url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACgAAAAoCAYAAACM/rhtAAAACXBIWXMAAAsTAAALEwEAmpwYAAAFR0lEQVR4nO2Ye4gbRRzH4wuVFhRBqeAfUgUF9Z8K/qNYr0apqIgorQql9rS29lrrtZSrSLHv1j7OV4sUEXziA1upbenb6+va3sPe9R5ce0lud2eTJrkku7lkZzePnXxlNpft5S45L9H9Q+jAh7CbmeWT/GZ+85t1ua61a83l2khw/xZ/1l0pjSFMdfz/2yyb720iZm6zzFApjTJjO2TzqKOC60m2dwMxUQ2NsonvQgw7AmaTY4LrxKywTsyiGrbLWfwUZpbkz0FdYZpwlFGOZAFKrqJz5L3QpdkVCa4eyAqrhSyqYTvJYvcgsyX3hChymogc5UgW0MkwMmDI/DMHPfD4uFJA/82g5DEkJfcabzq0ypdBNWyTMtgXYUWSu4NcUhiWFEtI+gEq15eXo/I9oMRb+IXbBAMfejNVsVXM4FCUjZH8rUhy1D9JZQNp6aFxBMnmwiD+gI8FHSs96arYImZwLMbKSGqWJNMEH6Pit4xKu6CTRmiBaeOHVycfFeT4fNnoo1hxOWWzf9BEZ5KV5c+YiYb+fN81nhROKqys5I8BqvDpNIGlMEIwTu5gmnjemsyagA0eiuWXUharPGkMmfhHtgppq//8LooNHmNcyUYpc7AiQVtUE6bAkKYu6zPk9/sMFNgbzqIjwcpyNGpi2aV83wU9FE+cjqGmWYH7bBmaY7m5LdoUV7VtcQ8VlvTqqJZX2xOoaVbxVLNSltputaZqwbpuKtT16HCSSJxY851pogSdTK9IcGG3JizspnCSiCrZaYdp0rkiARjCvaBSPZLkYesa7TdBE2qRGGgwE96GpT1J5Z0uCieJjBSk0oFiQUr68klSigHCLdCkBis/JX1gCS8aeuJ4u1OzmN+p4Xg0iwE9NyE6Eybqe6g19gshBQ9lGNAZfg9m7GdyBlURLClIjApfQRPvHi1IuCDTRAoEJzEqrL0q6MGK7jjmdWgWdV0U8QmkmaERfOIzrLHNimnfC6dz9jM5V2JiACC3lpxjoNKj0MlnSIpP50McnsySwloz6W3hgsu74ph7IWnzpZjCwXBmQvwSSOOtjvy4D3op9g/f3+Y1ip55JSaUFxyvsYT3UP3FOOb8lXSUsGpteZFCKZYvv6SdSAbvGl9wyLtzaaeKN9oTjhJW87tWiermSD7MKf8DMCQ3L6/y+NxI+BaZCQ9d0qnitbaExettCRwezMBH2Ri8lFkhraRfgbBSStCqDYdcMPyb8hfErmAK+zBfKHUdCma1DlnMu5AYd5GE0rmK+hUIKzzNjK4Prdrwexe3LBYsSHLBAbx7QcUrLUM2n/p0/BFMl2T9ZVpxP06IC+ZzYB+j0tfQyS5QspinPS54JF9q5+M+UjCnDWBBu4KXz8UdJaRIYFQ4BuDGsWkG4ckwyCJeqHIYlTgHCmGe36bgpbNxRwkpvJIWF1eWYqjQwyVr22J4oVm1eLFZxYFQGh6NTYh+jeEHYtjjyxFSCQ/vPgA3TEgOSfIIo9IQD3VtawzPn1EtZp+PV7yTBNM5e3w5gqo8vDiki9Dlz5EKPFgi1MFJ0Mk+UJIZuWDebI3iuTOKzZZ+ij2B1IRZ1ZssGl8KLjjqZOcfMx9h+BdZR77hFV2QnNsaxczTiqOEVH/xsVOXGRTfbcWCun+W9WX+8GxLzmmJ4NlTMUcJWYL2wZ3BIBtLhBjXQ5dXQpcPW68hhl9JzDoX1Z85GYOTfHP5ypP8/GOhCrdXtJrdTbFe94kYnGTmqcSdrmpbTVN0yYymSG7GiSicIfJr1XK25PHwfdObou7/mhnHBqe5gOv+teC19n9ufwPgOLKGoaqp6QAAAABJRU5ErkJggg=='); }");
                builder.append("</style>");
                builder.append("</head><body>");

                builder.append("<h1>").append(uri).append("</h1>");

                builder.append("<ul>");
                for (File file1 : files) {
                    //String fileLink1 = uri + (uri.endsWith("/") ? "" : "/") + file1.getName();
                    String fileLink1 = file1.getName();
                    builder.append("<li>");
                    if (file1.isDirectory()) {
                        builder.append("<a class='file-link' href='").append(fileLink1).append("/'><span class='folder-icon'></span>").append(file1.getName()).append("</a>");
                    } else {
                        String encodedFileLink = URLEncoder.encode(fileLink1, "UTF-8");
                        builder.append("<a class='file-link' href='").append(encodedFileLink).append("'><span class='file-icon'></span>").append(file1.getName()).append("</a>");
                    }
                    builder.append("</li>");
                }
                builder.append("</ul>");

                builder.append("</body></html>");

                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("text/html");
                response.getWriter().write(builder.toString());
                baseRequest.setHandled(true);
            }

            else if (file.exists()) {
                long fileSize = file.length();
                long startRange = 0;
                long endRange = fileSize - 1;

                String rangeHeader = request.getHeader("Range");
                if (rangeHeader != null) {
                    response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

                    String[] rangeValues = rangeHeader.substring(rangeHeader.indexOf("=") + 1).split("-");
                    startRange = Long.parseLong(rangeValues[0]);
                    if (rangeValues.length > 1) {
                        endRange = Math.min(Long.parseLong(rangeValues[1]), endRange);
                    }

                    response.setHeader("Content-Range", "bytes " + startRange + "-" + endRange + "/" + fileSize);
                } else {
                    response.setStatus(HttpServletResponse.SC_OK);
                }

                long contentLength = endRange - startRange + 1;
                response.setHeader("Content-Length", String.valueOf(contentLength));
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");

                FileInputStream inputStream = new FileInputStream(file);
                inputStream.skip(startRange);

                byte[] buffer = new byte[26048];
                int bytesRead;
                long bytesRemaining = contentLength;

                while (bytesRemaining > 0 && (bytesRead = inputStream.read(buffer, 0, (int) Math.min(buffer.length, bytesRemaining))) != -1) {
                    response.getOutputStream().write(buffer, 0, bytesRead);
                    bytesRemaining -= bytesRead;
                }

                inputStream.close();
                baseRequest.setHandled(true);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain");
                response.getWriter().write("File not found");
                baseRequest.setHandled(true);
            }
        }

    }
}
