package no.nav.foreldrepenger.oppdrag.web.app;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

@WebFilter(
    urlPatterns = {"/*"}
)
public class CorsFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletResponse instanceof HttpServletResponse) {
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            //TODO E149421 ikke tillatt alle origin, bare fpsak-gui
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, HEAD");
            response.setHeader("Access-Control-Allow-Headers", "Pragma, Expires, Cache-Control, Content-Type");
            response.setHeader("Access-Control-Allow-Credentials", "true");
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() {
    }
}
