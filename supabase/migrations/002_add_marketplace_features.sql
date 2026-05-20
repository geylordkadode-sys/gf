-- Add reviews table (if not already present, extend if needed)
CREATE TABLE IF NOT EXISTS public.reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
    buyer_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    seller_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    verified_purchase BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Add review replies table
CREATE TABLE IF NOT EXISTS public.review_replies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_id UUID NOT NULL REFERENCES public.reviews(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    reply_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Add follows table
CREATE TABLE IF NOT EXISTS public.follows (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    follower_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    followed_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(follower_id, followed_id)
);

-- Add notifications table
CREATE TABLE IF NOT EXISTS public.notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL, -- e.g., 'review', 'reply', 'message', 'report_status'
    source_id UUID, -- ID of the review, reply, message, etc.
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Add reports table
CREATE TABLE IF NOT EXISTS public.reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    reported_user_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
    reported_product_id UUID REFERENCES public.products(id) ON DELETE CASCADE,
    reported_review_id UUID REFERENCES public.reviews(id) ON DELETE CASCADE,
    report_type VARCHAR(50) NOT NULL, -- e.g., 'user', 'product', 'review', 'message'
    reason TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'pending', -- 'pending', 'reviewed', 'resolved'
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Add bans table
CREATE TABLE IF NOT EXISTS public.bans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    reason TEXT NOT NULL,
    banned_until TIMESTAMP,
    appeal_status VARCHAR(20) DEFAULT 'none', -- 'none', 'pending', 'approved', 'rejected'
    appeal_message TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Add kyc_applications table
CREATE TABLE IF NOT EXISTS public.kyc_applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    status VARCHAR(20) DEFAULT 'pending', -- 'pending', 'approved', 'rejected'
    document_type VARCHAR(50),
    document_url TEXT,
    full_name VARCHAR(255),
    address TEXT,
    date_of_birth DATE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Add device_accounts table to track accounts per device
CREATE TABLE IF NOT EXISTS public.device_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id TEXT NOT NULL,
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(device_id, user_id)
);

-- Add conversations table (if not already present, extend if needed)
CREATE TABLE IF NOT EXISTS public.conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    participant1_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    participant2_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    product_id UUID REFERENCES public.products(id) ON DELETE SET NULL, -- Product associated with the conversation
    last_message_at TIMESTAMP DEFAULT NOW(),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(participant1_id, participant2_id, product_id)
);

-- Add messages table (if not already present, extend if needed)
CREATE TABLE IF NOT EXISTS public.messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES public.conversations(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    receiver_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Add blocked_users table
CREATE TABLE IF NOT EXISTS public.blocked_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    blocker_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    blocked_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(blocker_id, blocked_id)
);

-- Add rate_limits table for post rate limiting
CREATE TABLE IF NOT EXISTS public.rate_limits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    feature VARCHAR(50) NOT NULL, -- e.g., 'post_product'
    count INTEGER DEFAULT 0,
    last_reset_at TIMESTAMP DEFAULT NOW(),
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, feature)
);

-- Update users table to add new fields
ALTER TABLE public.users
ADD COLUMN IF NOT EXISTS device_id TEXT, -- To track the last device used
ADD COLUMN IF NOT EXISTS is_banned BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS ban_reason TEXT,
ADD COLUMN IF NOT EXISTS ban_expires_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS kyc_status VARCHAR(20) DEFAULT 'none'; -- 'none', 'pending', 'approved', 'rejected'

-- Enable RLS for new tables
ALTER TABLE public.reviews ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.review_replies ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.follows ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.bans ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.kyc_applications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.device_accounts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.blocked_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.rate_limits ENABLE ROW LEVEL SECURITY;

-- RLS Policies for new tables

-- Reviews
CREATE POLICY reviews_select_all ON public.reviews
    FOR SELECT USING (TRUE);
CREATE POLICY reviews_insert_own ON public.reviews
    FOR INSERT WITH CHECK (auth.uid() = buyer_id);
CREATE POLICY reviews_update_own ON public.reviews
    FOR UPDATE USING (auth.uid() = buyer_id OR auth.uid() = seller_id);
CREATE POLICY reviews_delete_own ON public.reviews
    FOR DELETE USING (auth.uid() = buyer_id);

-- Review Replies
CREATE POLICY review_replies_select_all ON public.review_replies
    FOR SELECT USING (TRUE);
CREATE POLICY review_replies_insert_own ON public.review_replies
    FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY review_replies_update_own ON public.review_replies
    FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY review_replies_delete_own ON public.review_replies
    FOR DELETE USING (auth.uid() = user_id);

-- Follows
CREATE POLICY follows_select_own ON public.follows
    FOR SELECT USING (auth.uid() = follower_id OR auth.uid() = followed_id);
CREATE POLICY follows_insert_own ON public.follows
    FOR INSERT WITH CHECK (auth.uid() = follower_id);
CREATE POLICY follows_delete_own ON public.follows
    FOR DELETE USING (auth.uid() = follower_id);

-- Notifications
CREATE POLICY notifications_select_own ON public.notifications
    FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY notifications_insert_own ON public.notifications
    FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY notifications_update_own ON public.notifications
    FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY notifications_delete_own ON public.notifications
    FOR DELETE USING (auth.uid() = user_id);

-- Reports
CREATE POLICY reports_insert_own ON public.reports
    FOR INSERT WITH CHECK (auth.uid() = reporter_id);
-- Admin can view all reports, reporter can view their own
CREATE POLICY reports_select_all ON public.reports
    FOR SELECT USING (auth.uid() = reporter_id OR EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));
CREATE POLICY reports_update_admin ON public.reports
    FOR UPDATE USING (EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));

-- Bans
-- Admin can view and manage bans
CREATE POLICY bans_select_admin ON public.bans
    FOR SELECT USING (EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE) OR auth.uid() = user_id);
CREATE POLICY bans_insert_admin ON public.bans
    FOR INSERT WITH CHECK (EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));
CREATE POLICY bans_update_admin ON public.bans
    FOR UPDATE USING (EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));

-- KYC Applications
CREATE POLICY kyc_applications_select_own ON public.kyc_applications
    FOR SELECT USING (auth.uid() = user_id OR EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));
CREATE POLICY kyc_applications_insert_own ON public.kyc_applications
    FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY kyc_applications_update_own ON public.kyc_applications
    FOR UPDATE USING (auth.uid() = user_id OR EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));

-- Device Accounts
CREATE POLICY device_accounts_select_own ON public.device_accounts
    FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY device_accounts_insert_own ON public.device_accounts
    FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY device_accounts_delete_own ON public.device_accounts
    FOR DELETE USING (auth.uid() = user_id);

-- Conversations
CREATE POLICY conversations_select_own ON public.conversations
    FOR SELECT USING (auth.uid() = participant1_id OR auth.uid() = participant2_id);
CREATE POLICY conversations_insert_own ON public.conversations
    FOR INSERT WITH CHECK (auth.uid() = participant1_id OR auth.uid() = participant2_id);
CREATE POLICY conversations_update_own ON public.conversations
    FOR UPDATE USING (auth.uid() = participant1_id OR auth.uid() = participant2_id);

-- Messages
CREATE POLICY messages_select_own ON public.messages
    FOR SELECT USING (auth.uid() = sender_id OR auth.uid() = receiver_id);
CREATE POLICY messages_insert_own ON public.messages
    FOR INSERT WITH CHECK (auth.uid() = sender_id);
CREATE POLICY messages_update_own ON public.messages
    FOR UPDATE USING (auth.uid() = sender_id OR auth.uid() = receiver_id);

-- Blocked Users
CREATE POLICY blocked_users_select_own ON public.blocked_users
    FOR SELECT USING (auth.uid() = blocker_id OR auth.uid() = blocked_id);
CREATE POLICY blocked_users_insert_own ON public.blocked_users
    FOR INSERT WITH CHECK (auth.uid() = blocker_id);
CREATE POLICY blocked_users_delete_own ON public.blocked_users
    FOR DELETE USING (auth.uid() = blocker_id);

-- Rate Limits
CREATE POLICY rate_limits_select_own ON public.rate_limits
    FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY rate_limits_insert_own ON public.rate_limits
    FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY rate_limits_update_own ON public.rate_limits
    FOR UPDATE USING (auth.uid() = user_id);

-- Add is_admin column to users table for reports/bans RLS
ALTER TABLE public.users
ADD COLUMN IF NOT EXISTS is_admin BOOLEAN DEFAULT FALSE;

-- Add total_reviews and average_rating to products table
ALTER TABLE public.products
ADD COLUMN IF NOT EXISTS total_reviews INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS average_rating NUMERIC(2, 1) DEFAULT 0.0;

-- Add last_activity_at to users table for tracking active users
ALTER TABLE public.users
ADD COLUMN IF NOT EXISTS last_activity_at TIMESTAMP DEFAULT NOW();

-- Add is_online to users table
ALTER TABLE public.users
ADD COLUMN IF NOT EXISTS is_online BOOLEAN DEFAULT FALSE;

-- Add product_count to users table
ALTER TABLE public.users
ADD COLUMN IF NOT EXISTS product_count INTEGER DEFAULT 0;

-- Add language_preference to users table
ALTER TABLE public.users
ADD COLUMN IF NOT EXISTS language_preference VARCHAR(10) DEFAULT 'en';

-- Add indexes for new tables
CREATE INDEX IF NOT EXISTS idx_reviews_product_id ON public.reviews(product_id);
CREATE INDEX IF NOT EXISTS idx_reviews_buyer_id ON public.reviews(buyer_id);
CREATE INDEX IF NOT EXISTS idx_reviews_seller_id ON public.reviews(seller_id);
CREATE INDEX IF NOT EXISTS idx_review_replies_review_id ON public.review_replies(review_id);
CREATE INDEX IF NOT EXISTS idx_review_replies_user_id ON public.review_replies(user_id);
CREATE INDEX IF NOT EXISTS idx_follows_follower_id ON public.follows(follower_id);
CREATE INDEX IF NOT EXISTS idx_follows_followed_id ON public.follows(followed_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON public.notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_reports_reporter_id ON public.reports(reporter_id);
CREATE INDEX IF NOT EXISTS idx_reports_reported_user_id ON public.reports(reported_user_id);
CREATE INDEX IF NOT EXISTS idx_reports_reported_product_id ON public.reports(reported_product_id);
CREATE INDEX IF NOT EXISTS idx_bans_user_id ON public.bans(user_id);
CREATE INDEX IF NOT EXISTS idx_kyc_applications_user_id ON public.kyc_applications(user_id);
CREATE INDEX IF NOT EXISTS idx_device_accounts_device_id ON public.device_accounts(device_id);
CREATE INDEX IF NOT EXISTS idx_conversations_participant1_id ON public.conversations(participant1_id);
CREATE INDEX IF NOT EXISTS idx_conversations_participant2_id ON public.conversations(participant2_id);
CREATE INDEX IF NOT EXISTS idx_messages_conversation_id ON public.messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_messages_sender_id ON public.messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_receiver_id ON public.messages(receiver_id);
CREATE INDEX IF NOT EXISTS idx_blocked_users_blocker_id ON public.blocked_users(blocker_id);
CREATE INDEX IF NOT EXISTS idx_blocked_users_blocked_id ON public.blocked_users(blocked_id);
CREATE INDEX IF NOT EXISTS idx_rate_limits_user_id ON public.rate_limits(user_id);

-- Add reviews table (if not already present, extend if needed)
CREATE TABLE IF NOT EXISTS public.reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
    buyer_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    seller_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    verified_purchase BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Add review replies table
CREATE TABLE IF NOT EXISTS public.review_replies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_id UUID NOT NULL REFERENCES public.reviews(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    reply_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Add follows table
CREATE TABLE IF NOT EXISTS public.follows (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    follower_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    followed_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(follower_id, followed_id)
);

-- Add notifications table
CREATE TABLE IF NOT EXISTS public.notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL, -- e.g., 'review', 'reply', 'message', 'report_status'
    source_id UUID, -- ID of the review, reply, message, etc.
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Add reports table
CREATE TABLE IF NOT EXISTS public.reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    reported_user_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
    reported_product_id UUID REFERENCES public.products(id) ON DELETE CASCADE,
    reported_review_id UUID REFERENCES public.reviews(id) ON DELETE CASCADE,
    report_type VARCHAR(50) NOT NULL, -- e.g., 'user', 'product', 'review', 'message'
    reason TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'pending', -- 'pending', 'reviewed', 'resolved'
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Add bans table
CREATE TABLE IF NOT EXISTS public.bans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    reason TEXT NOT NULL,
    banned_until TIMESTAMP,
    appeal_status VARCHAR(20) DEFAULT 'none', -- 'none', 'pending', 'approved', 'rejected'
    appeal_message TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Add kyc_applications table
CREATE TABLE IF NOT EXISTS public.kyc_applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    status VARCHAR(20) DEFAULT 'pending', -- 'pending', 'approved', 'rejected'
    document_type VARCHAR(50),
    document_url TEXT,
    full_name VARCHAR(255),
    address TEXT,
    date_of_birth DATE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Add device_accounts table to track accounts per device
CREATE TABLE IF NOT EXISTS public.device_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id TEXT NOT NULL,
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(device_id, user_id)
);

-- Add conversations table (if not already present, extend if needed)
CREATE TABLE IF NOT EXISTS public.conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    participant1_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    participant2_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    product_id UUID REFERENCES public.products(id) ON DELETE SET NULL, -- Product associated with the conversation
    last_message_at TIMESTAMP DEFAULT NOW(),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(participant1_id, participant2_id, product_id)
);

-- Add messages table (if not already present, extend if needed)
CREATE TABLE IF NOT EXISTS public.messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES public.conversations(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    receiver_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Add blocked_users table
CREATE TABLE IF NOT EXISTS public.blocked_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    blocker_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    blocked_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(blocker_id, blocked_id)
);

-- Add rate_limits table for post rate limiting
CREATE TABLE IF NOT EXISTS public.rate_limits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    feature VARCHAR(50) NOT NULL, -- e.g., 'post_product'
    count INTEGER DEFAULT 0,
    last_reset_at TIMESTAMP DEFAULT NOW(),
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, feature)
);

-- Update users table to add new fields
ALTER TABLE public.users
ADD COLUMN IF NOT EXISTS device_id TEXT, -- To track the last device used
ADD COLUMN IF NOT EXISTS is_banned BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS ban_reason TEXT,
ADD COLUMN IF NOT EXISTS ban_expires_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS kyc_status VARCHAR(20) DEFAULT 'none'; -- 'none', 'pending', 'approved', 'rejected'

-- Enable RLS for new tables
ALTER TABLE public.reviews ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.review_replies ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.follows ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.bans ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.kyc_applications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.device_accounts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.blocked_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.rate_limits ENABLE ROW LEVEL SECURITY;

-- RLS Policies for new tables

-- Reviews
CREATE POLICY reviews_select_all ON public.reviews
    FOR SELECT USING (TRUE);
CREATE POLICY reviews_insert_own ON public.reviews
    FOR INSERT WITH CHECK (auth.uid() = buyer_id);
CREATE POLICY reviews_update_own ON public.reviews
    FOR UPDATE USING (auth.uid() = buyer_id OR auth.uid() = seller_id);
CREATE POLICY reviews_delete_own ON public.reviews
    FOR DELETE USING (auth.uid() = buyer_id);

-- Review Replies
CREATE POLICY review_replies_select_all ON public.review_replies
    FOR SELECT USING (TRUE);
CREATE POLICY review_replies_insert_own ON public.review_replies
    FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY review_replies_update_own ON public.review_replies
    FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY review_replies_delete_own ON public.review_replies
    FOR DELETE USING (auth.uid() = user_id);

-- Follows
CREATE POLICY follows_select_own ON public.follows
    FOR SELECT USING (auth.uid() = follower_id OR auth.uid() = followed_id);
CREATE POLICY follows_insert_own ON public.follows
    FOR INSERT WITH CHECK (auth.uid() = follower_id);
CREATE POLICY follows_delete_own ON public.follows
    FOR DELETE USING (auth.uid() = follower_id);

-- Notifications
CREATE POLICY notifications_select_own ON public.notifications
    FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY notifications_insert_own ON public.notifications
    FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY notifications_update_own ON public.notifications
    FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY notifications_delete_own ON public.notifications
    FOR DELETE USING (auth.uid() = user_id);

-- Reports
CREATE POLICY reports_insert_own ON public.reports
    FOR INSERT WITH CHECK (auth.uid() = reporter_id);
-- Admin can view all reports, reporter can view their own
CREATE POLICY reports_select_all ON public.reports
    FOR SELECT USING (auth.uid() = reporter_id OR EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));
CREATE POLICY reports_update_admin ON public.reports
    FOR UPDATE USING (EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));

-- Bans
-- Admin can view and manage bans
CREATE POLICY bans_select_admin ON public.bans
    FOR SELECT USING (EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE) OR auth.uid() = user_id);
CREATE POLICY bans_insert_admin ON public.bans
    FOR INSERT WITH CHECK (EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));
CREATE POLICY bans_update_admin ON public.bans
    FOR UPDATE USING (EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));

-- KYC Applications
CREATE POLICY kyc_applications_select_own ON public.kyc_applications
    FOR SELECT USING (auth.uid() = user_id OR EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));
CREATE POLICY kyc_applications_insert_own ON public.kyc_applications
    FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY kyc_applications_update_own ON public.kyc_applications
    FOR UPDATE USING (auth.uid() = user_id OR EXISTS (SELECT 1 FROM public.users WHERE id = auth.uid() AND is_admin = TRUE));

-- Device Accounts
CREATE POLICY device_accounts_select_own ON public.device_accounts
    FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY device_accounts_insert_own ON public.device_accounts
    FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY device_accounts_delete_own ON public.device_accounts
    FOR DELETE USING (auth.uid() = user_id);

-- Conversations
CREATE POLICY conversations_select_own ON public.conversations
    FOR SELECT USING (auth.uid() = participant1_id OR auth.uid() = participant2_id);
CREATE POLICY conversations_insert_own ON public.conversations
    FOR INSERT WITH CHECK (auth.uid() = participant1_id OR auth.uid() = participant2_id);
CREATE POLICY conversations_update_own ON public.conversations
    FOR UPDATE USING (auth.uid() = participant1_id OR auth.uid() = participant2_id);

-- Messages
CREATE POLICY messages_select_own ON public.messages
    FOR SELECT USING (auth.uid() = sender_id OR auth.uid() = receiver_id);
CREATE POLICY messages_insert_own ON public.messages
    FOR INSERT WITH CHECK (auth.uid() = sender_id);
CREATE POLICY messages_update_own ON public.messages
    FOR UPDATE USING (auth.uid() = sender_id OR auth.uid() = receiver_id);

-- Blocked Users
CREATE POLICY blocked_users_select_own ON public.blocked_users
    FOR SELECT USING (auth.uid() = blocker_id OR auth.uid() = blocked_id);
CREATE POLICY blocked_users_insert_own ON public.blocked_users
    FOR INSERT WITH CHECK (auth.uid() = blocker_id);
CREATE POLICY blocked_users_delete_own ON public.blocked_users
    FOR DELETE USING (auth.uid() = blocker_id);

-- Rate Limits
CREATE POLICY rate_limits_select_own ON public.rate_limits
    FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY rate_limits_insert_own ON public.rate_limits
    FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY rate_limits_update_own ON public.rate_limits
    FOR UPDATE USING (auth.uid() = user_id);

-- Add is_admin column to users table for reports/bans RLS
ALTER TABLE public.users
ADD COLUMN IF NOT EXISTS is_admin BOOLEAN DEFAULT FALSE;

-- Add total_reviews and average_rating to products table
ALTER TABLE public.products
ADD COLUMN IF NOT EXISTS total_reviews INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS average_rating NUMERIC(2, 1) DEFAULT 0.0;

-- Add last_activity_at to users table for tracking active users
ALTER TABLE public.users
ADD COLUMN IF NOT EXISTS last_activity_at TIMESTAMP DEFAULT NOW();

-- Add is_online to users table
ALTER TABLE public.users
ADD COLUMN IF NOT EXISTS is_online BOOLEAN DEFAULT FALSE;

-- Add product_count to users table
ALTER TABLE public.users
ADD COLUMN IF NOT EXISTS product_count INTEGER DEFAULT 0;

-- Add language_preference to users table
ALTER TABLE public.users
ADD COLUMN IF NOT EXISTS language_preference VARCHAR(10) DEFAULT 'en';

-- Add indexes for new tables
CREATE INDEX IF NOT EXISTS idx_reviews_product_id ON public.reviews(product_id);
CREATE INDEX IF NOT EXISTS idx_reviews_buyer_id ON public.reviews(buyer_id);
CREATE INDEX IF NOT EXISTS idx_reviews_seller_id ON public.reviews(seller_id);
CREATE INDEX IF NOT EXISTS idx_review_replies_review_id ON public.review_replies(review_id);
CREATE INDEX IF NOT EXISTS idx_review_replies_user_id ON public.review_replies(user_id);
CREATE INDEX IF NOT EXISTS idx_follows_follower_id ON public.follows(follower_id);
CREATE INDEX IF NOT EXISTS idx_follows_followed_id ON public.follows(followed_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON public.notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_reports_reporter_id ON public.reports(reporter_id);
CREATE INDEX IF NOT EXISTS idx_reports_reported_user_id ON public.reports(reported_user_id);
CREATE INDEX IF NOT EXISTS idx_reports_reported_product_id ON public.reports(reported_product_id);
CREATE INDEX IF NOT EXISTS idx_bans_user_id ON public.bans(user_id);
CREATE INDEX IF NOT EXISTS idx_kyc_applications_user_id ON public.kyc_applications(user_id);
CREATE INDEX IF NOT EXISTS idx_device_accounts_device_id ON public.device_accounts(device_id);
CREATE INDEX IF NOT EXISTS idx_conversations_participant1_id ON public.conversations(participant1_id);
CREATE INDEX IF NOT EXISTS idx_conversations_participant2_id ON public.conversations(participant2_id);
CREATE INDEX IF NOT EXISTS idx_messages_conversation_id ON public.messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_messages_sender_id ON public.messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_receiver_id ON public.messages(receiver_id);
CREATE INDEX IF NOT EXISTS idx_blocked_users_blocker_id ON public.blocked_users(blocker_id);
CREATE INDEX IF NOT EXISTS idx_blocked_users_blocked_id ON public.blocked_users(blocked_id);
CREATE INDEX IF NOT EXISTS idx_rate_limits_user_id ON public.rate_limits(user_id);
