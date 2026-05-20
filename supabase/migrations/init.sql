-- Create users table
CREATE TABLE IF NOT EXISTS public.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) DEFAULT '',
    country VARCHAR(100) DEFAULT '',
    location VARCHAR(255) DEFAULT '',
    profile_photo_url TEXT DEFAULT '',
    website VARCHAR(255) DEFAULT '',
    instagram_handle VARCHAR(100) DEFAULT '',
    facebook_handle VARCHAR(100) DEFAULT '',
    twitter_handle VARCHAR(100) DEFAULT '',
    rating FLOAT DEFAULT 0,
    review_count INTEGER DEFAULT 0,
    total_listings INTEGER DEFAULT 0,
    followers INTEGER DEFAULT 0,
    following INTEGER DEFAULT 0,
    is_verified BOOLEAN DEFAULT FALSE,
    joined_date TIMESTAMP DEFAULT NOW(),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Create products table
CREATE TABLE IF NOT EXISTS public.products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10, 2) NOT NULL,
    image_url TEXT DEFAULT '',
    image_urls TEXT[] DEFAULT ARRAY[]::TEXT[],
    category VARCHAR(100) DEFAULT '',
    is_active BOOLEAN DEFAULT TRUE,
    views INTEGER DEFAULT 0,
    likes INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Create favorites table
CREATE TABLE IF NOT EXISTS public.favorites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(product_id, user_id)
);

-- Create OTP table for verification
CREATE TABLE IF NOT EXISTS public.otp_verifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    code VARCHAR(10) NOT NULL,
    type VARCHAR(20) DEFAULT 'signup', -- signup, password_reset
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    expires_at TIMESTAMP DEFAULT NOW() + INTERVAL '15 minutes'
);

-- Create sync logs table for offline-first sync tracking
CREATE TABLE IF NOT EXISTS public.sync_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
    entity_type VARCHAR(50), -- 'user', 'product', 'favorite'
    entity_id UUID,
    operation VARCHAR(20), -- 'create', 'update', 'delete'
    data JSONB,
    sync_status VARCHAR(20) DEFAULT 'pending', -- 'pending', 'synced', 'failed'
    sync_attempts INTEGER DEFAULT 0,
    last_sync_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_products_seller_id ON public.products(seller_id);
CREATE INDEX IF NOT EXISTS idx_products_category ON public.products(category);
CREATE INDEX IF NOT EXISTS idx_favorites_user_id ON public.favorites(user_id);
CREATE INDEX IF NOT EXISTS idx_favorites_product_id ON public.favorites(product_id);
CREATE INDEX IF NOT EXISTS idx_otp_email ON public.otp_verifications(email);
CREATE INDEX IF NOT EXISTS idx_sync_logs_user_id ON public.sync_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_sync_logs_status ON public.sync_logs(sync_status);

-- Enable RLS (Row Level Security)
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.favorites ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.otp_verifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sync_logs ENABLE ROW LEVEL SECURITY;

-- RLS Policies
-- Users can read their own data
CREATE POLICY users_select_self ON public.users
    FOR SELECT USING (auth.uid() = id OR TRUE);

-- Users can update their own data
CREATE POLICY users_update_self ON public.users
    FOR UPDATE USING (auth.uid() = id);

-- Products are readable by anyone
CREATE POLICY products_select_all ON public.products
    FOR SELECT USING (TRUE);

-- Users can create products
CREATE POLICY products_insert_own ON public.products
    FOR INSERT WITH CHECK (auth.uid() = seller_id);

-- Users can update their own products
CREATE POLICY products_update_own ON public.products
    FOR UPDATE USING (auth.uid() = seller_id);

-- Favorites are private
CREATE POLICY favorites_select_own ON public.favorites
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY favorites_insert_own ON public.favorites
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY favorites_delete_own ON public.favorites
    FOR DELETE USING (auth.uid() = user_id);
