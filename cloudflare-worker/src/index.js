export default {
  async fetch(request, env, ctx) {
    // Handle CORS preflight
    if (request.method === "OPTIONS") {
      return new Response(null, {
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "POST, OPTIONS",
          "Access-Control-Allow-Headers": "Content-Type",
          "Access-Control-Max-Age": "86400",
        }
      });
    }

    if (request.method !== "POST") {
      return new Response("Method not allowed", { status: 405 });
    }

    try {
      const { items } = await request.json(); // Array of { id, type: 'movie' | 'tv' }
      if (!Array.isArray(items)) {
        return new Response("Invalid request", { status: 400 });
      }

      const results = {};

      // Check all IDs in parallel
      await Promise.all(items.map(async (item) => {
        const id = item.id;
        const type = item.type || 'movie';
        
        // For TV shows, we just check Season 1 Episode 1
        const url = type === 'tv' 
          ? \`https://vidlink.pro/tv/\${id}/1/1\`
          : \`https://vidlink.pro/movie/\${id}\`;

        try {
          // Make a fast HEAD request
          const res = await fetch(url, {
            method: "HEAD",
            headers: {
              "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
            }
          });
          
          // 200 OK means it's available. 404/500 means it doesn't exist.
          results[id] = res.status === 200;
        } catch (e) {
          // If fetch fails completely, assume unavailable
          results[id] = false;
        }
      }));

      return new Response(JSON.stringify(results), {
        headers: {
          "Content-Type": "application/json",
          "Access-Control-Allow-Origin": "*"
        }
      });
    } catch (e) {
      return new Response(JSON.stringify({ error: e.message }), { 
        status: 500,
        headers: {
          "Content-Type": "application/json",
          "Access-Control-Allow-Origin": "*"
        }
      });
    }
  }
};
