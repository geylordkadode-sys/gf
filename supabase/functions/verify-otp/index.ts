import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!

const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY)

serve(async (req) => {
  if (req.method !== "POST") {
    return new Response(JSON.stringify({ error: "Method not allowed" }), {
      status: 405,
    })
  }

  try {
    const { email, code, type = "signup" } = await req.json()

    if (!email || !code) {
      return new Response(
        JSON.stringify({ error: "Email and code are required" }),
        { status: 400 }
      )
    }

    // Verify OTP
    const { data: otpData, error: otpError } = await supabase
      .from("otp_codes")
      .select("*")
      .eq("email", email)
      .eq("code", code)
      .eq("type", type)
      .gte("expires_at", new Date().toISOString())
      .single()

    if (otpError || !otpData) {
      return new Response(JSON.stringify({ error: "Invalid or expired OTP" }), {
        status: 400,
      })
    }

    // Delete used OTP
    await supabase.from("otp_codes").delete().eq("id", otpData.id)

    return new Response(
      JSON.stringify({
        success: true,
        message: "OTP verified successfully",
      }),
      { status: 200, headers: { "Content-Type": "application/json" } }
    )
  } catch (error) {
    return new Response(JSON.stringify({ error: error.message }), {
      status: 500,
      headers: { "Content-Type": "application/json" },
    })
  }
})
