import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!

const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY)

function generateOTP(): string {
  return Math.floor(100000 + Math.random() * 900000).toString()
}

serve(async (req) => {
  if (req.method !== "POST") {
    return new Response(JSON.stringify({ error: "Method not allowed" }), {
      status: 405,
    })
  }

  try {
    const { email, type = "signup" } = await req.json()

    if (!email) {
      return new Response(JSON.stringify({ error: "Email is required" }), {
        status: 400,
      })
    }

    const otp = generateOTP()
    const expiresAt = new Date(Date.now() + 10 * 60 * 1000) // 10 minutes

    // Store OTP in database
    const { error: dbError } = await supabase
      .from("otp_codes")
      .insert({
        email,
        code: otp,
        type,
        expires_at: expiresAt.toISOString(),
      })

    if (dbError) {
      console.error("Database error:", dbError)
      return new Response(
        JSON.stringify({ error: "Failed to generate OTP" }),
        { status: 500 }
      )
    }

    // Send email (integrate with Resend or your email service)
    console.log(`OTP for ${email}: ${otp}`)

    return new Response(
      JSON.stringify({
        success: true,
        message: "OTP sent successfully",
      }),
      { status: 200, headers: { "Content-Type": "application/json" } }
    );
  } catch (error) {
    return new Response(JSON.stringify({ error: error.message }), {
      status: 500,
      headers: { "Content-Type": "application/json" },
    });
  }
});
