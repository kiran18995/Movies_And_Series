'use strict';

const path = require('path');
const express = require('express');
const { createProxyMiddleware, responseInterceptor } = require('http-proxy-middleware');

const app = express();
const PORT = 8080;

// ── Serve the web app (index.html, app.js, style.css) ────────
app.use(express.static(path.join(__dirname)));

// ── Reverse proxy: /stream/* → https://streamimdb.ru/* ───────
// This is the same URL the Android app uses in its WebView.
// We strip X-Frame-Options / CSP frame-ancestors so the embed
// renders inside our <iframe> without being blocked by the browser.
// We also rewrite root-relative paths in the HTML response so that
// all sub-resources (JS, CSS, API calls) are also routed through
// this proxy (keeping the same-origin relationship intact).
app.use(
  '/stream',
  createProxyMiddleware({
    target: 'https://streamimdb.ru',
    changeOrigin: true,
    secure: false,
    followRedirects: true,
    pathRewrite: { '^/stream': '' },
    selfHandleResponse: true,          // required for body rewriting

    // ── Mimic a real browser so the site serves embed content ─
    onProxyReq(proxyReq) {
      proxyReq.setHeader(
        'User-Agent',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ' +
        '(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36'
      );
      proxyReq.setHeader('Accept', 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8');
      proxyReq.setHeader('Accept-Language', 'en-US,en;q=0.5');
      proxyReq.setHeader('Referer', 'https://streamimdb.ru/');
      proxyReq.setHeader('Origin', 'https://streamimdb.ru');
    },

    // ── Intercept + rewrite the response ─────────────────────
    onProxyRes: responseInterceptor(async (responseBuffer, proxyRes, req, res) => {
      // 1. Remove frame-blocking headers from the proxied response
      res.removeHeader('x-frame-options');
      res.removeHeader('X-Frame-Options');
      const csp = res.getHeader('content-security-policy');
      if (csp) {
        const cleaned = String(csp)
          .replace(/frame-ancestors\s+[^;]*(;|$)/gi, '')
          .trim();
        if (cleaned) res.setHeader('content-security-policy', cleaned);
        else res.removeHeader('content-security-policy');
      }
      res.removeHeader('content-security-policy-report-only');

      // 2. Only rewrite HTML bodies
      const ct = (proxyRes.headers['content-type'] || '').toLowerCase();
      if (!ct.includes('text/html')) return responseBuffer;

      let html = responseBuffer.toString('utf-8');

      // Rewrite root-relative src/href/action/data-src attributes so that
      // assets served at /foo on streamimdb.ru are fetched via /stream/foo
      // on our local server (keeping the iframe same-origin with the proxy).
      html = html
        .replace(/((?:src|href|action|data-src)\s*=\s*["'])\/(?!\/)/g, '$1/stream/')
        .replace(/url\(\s*['"]\/(?!\/)/g, "url('/stream/")
        .replace(/url\(\/(?!\/)/g, 'url(/stream/');

      return Buffer.from(html, 'utf-8');
    }),
  })
);

app.listen(PORT, () => {
  console.log(`\n🎬  Movies & Series  →  http://localhost:${PORT}\n`);
});
