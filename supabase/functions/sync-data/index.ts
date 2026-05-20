// Supabase Edge Function for syncing offline changes
// Deploy with: supabase functions deploy sync-data

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const supabase = createClient(
  Deno.env.get("SUPABASE_URL") || "",
  Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") || ""
);

serve(async (req) => {
  if (req.method !== "POST") {
    return new Response(JSON.stringify({ error: "Method not allowed" }), {
      status: 405,
    });
  }

  try {
    const authHeader = req.headers.get("Authorization");
    if (!authHeader) {
      return new Response(JSON.stringify({ error: "Unauthorized" }), {
        status: 401,
      });
    }

    const token = authHeader.replace("Bearer ", "");
    const {
      data: { user },
    } = await supabase.auth.getUser(token);

    if (!user) {
      return new Response(JSON.stringify({ error: "User not found" }), {
        status: 401,
      });
    }

    const { syncs } = await req.json();

    if (!syncs || !Array.isArray(syncs)) {
      return new Response(
        JSON.stringify({ error: "Invalid sync data format" }),
        { status: 400 }
      );
    }

    const results = [];

    for (const sync of syncs) {
      try {
        const { entityType, operation, data, entityId } = sync;

        switch (entityType) {
          case "product":
            if (operation === "create") {
              const { error } = await supabase
                .from("products")
                .insert({ ...data, seller_id: user.id });
              results.push({
                entityId,
                success: !error,
                error: error?.message,
              });
            } else if (operation === "update") {
              const { error } = await supabase
                .from("products")
                .update(data)
                .eq("id", entityId)
                .eq("seller_id", user.id);
              results.push({
                entityId,
                success: !error,
                error: error?.message,
              });
            }
            break;

          case "favorite":
            if (operation === "create") {
              const { error } = await supabase
                .from("favorites")
                .insert({ ...data, user_id: user.id });
              results.push({
                entityId,
                success: !error,
                error: error?.message,
              });
            } else if (operation === "delete") {
              const { error } = await supabase
                .from("favorites")
                .delete()
                .eq("id", entityId)
                .eq("user_id", user.id);
              results.push({
                entityId,
                success: !error,
                error: error?.message,
              });
            }
            break;
        }
      } catch (error) {
        results.push({
          entityId: sync.entityId,
          success: false,
          error: error.message,
        });
      }
    }

    return new Response(JSON.stringify({ results }), {
      status: 200,
      headers: { "Content-Type": "application/json" },
    });
  } catch (error) {
    return new Response(JSON.stringify({ error: error.message }), {
      status: 500,
      headers: { "Content-Type": "application/json" },
    });
  }
});
