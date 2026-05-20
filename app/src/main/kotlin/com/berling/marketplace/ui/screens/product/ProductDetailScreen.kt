@file:OptIn(ExperimentalMaterial3Api::class)
package com.berling.marketplace.ui.screens.product

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.berling.marketplace.ui.screens.UiState

@Composable
fun ProductDetailScreen(
    navController: NavHostController,
    productId: String = "",
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val productDetailState by viewModel.productDetailState.collectAsState()
    val isFavorite by viewModel.isFavoriteState.collectAsState()
    val selectedImageIndex by viewModel.selectedImageIndex.collectAsState()

    LaunchedEffect(productId) {
        if (productId.isNotEmpty()) {
            viewModel.loadProductDetail(productId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    BadgedBox(badge = { Badge { Text("2") } }) {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                        }
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = { }
            )
        }
    ) { paddingValues ->
        when (val state = productDetailState) {
            UiState.Idle, UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Success -> {
                val detailData = state.data
                ProductDetailContent(
                    detailData = detailData,
                    selectedImageIndex = selectedImageIndex,
                    isFavorite = isFavorite,
                    onImageSelected = { viewModel.setSelectedImage(it) },
                    onFavoriteToggle = { viewModel.toggleFavorite(detailData.product.id) },
                    onFollowSeller = { viewModel.followSeller(detailData.seller.id) },
                    onAddToCart = { viewModel.addToCart(detailData.product.id) },
                    onBuyNow = { viewModel.buyNow(detailData.product.id) },
                    onViewSellerShop = { navController.navigate("seller_profile/${detailData.seller.id}") },
                    modifier = Modifier.padding(paddingValues),
                    navController = navController
                )
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message)
                }
            }
        }
    }
}

@Composable
private fun ProductDetailContent(
    detailData: ProductDetailData,
    selectedImageIndex: Int,
    isFavorite: Boolean,
    onImageSelected: (Int) -> Unit,
    onFavoriteToggle: () -> Unit,
    onFollowSeller: () -> Unit,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit,
    onViewSellerShop: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ProductImageCarousel(
            product = detailData.product,
            selectedImageIndex = selectedImageIndex,
            onImageSelected = onImageSelected,
            isFavorite = isFavorite,
            onFavoriteToggle = onFavoriteToggle,
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
        )

        ProductInfoSection(
            product = detailData.product,
            ratings = detailData.ratings,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        ProductFeaturesSection(modifier = Modifier.padding(horizontal = 16.dp))

        ColorSelectionSection(modifier = Modifier.padding(16.dp))

        ProductDetailsSection(
            product = detailData.product,
            modifier = Modifier.padding(16.dp)
        )

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        SellerInfoCard(
            seller = detailData.seller,
            sellerRating = detailData.sellerRating,
            responseRate = detailData.responseRate,
            followersCount = detailData.followersCount,
            onFollow = onFollowSeller,
            onViewShop = onViewSellerShop,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        RatingsAndReviewsSection(
            ratings = detailData.ratings,
            reviews = detailData.reviews,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        WriteReviewButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        MoreFromSellerSection(
            products = detailData.relatedProducts,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        ActionButtonsSection(
            onMessage = {
                navController.navigate(
                    "chat_with_seller/${detailData.seller.id}/${detailData.seller.name}/${detailData.product.id}/${detailData.product.title}"
                )
            },
            onFollow = onFollowSeller,
            onShare = { },
            onReportSeller = { },
            onBlockSeller = { }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProductImageCarousel(
    product: com.berling.marketplace.data.local.entities.ProductEntity,
    selectedImageIndex: Int,
    onImageSelected: (Int) -> Unit,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
        // Main image
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // Favorite button
        IconButton(
            onClick = onFavoriteToggle,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape
                )
        ) {
            Icon(
                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
            )
        }

        // Image counter
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                "${selectedImageIndex + 1}/6",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProductInfoSection(
    product: com.berling.marketplace.data.local.entities.ProductEntity,
    ratings: ProductRatings,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistChip(
                onClick = { },
                label = { Text("Best Seller") },
                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, Modifier.size(16.dp)) }
            )
        }

        Text(
            product.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Price section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "₹${product.price}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "₹${(product.price * 1.93).toInt()}",
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = TextDecoration.LineThrough,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                "48% OFF",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Rating and sold count
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                "4.7",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "(${ratings.totalReviews} Reviews)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "512+ Sold",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProductFeaturesSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FeatureItem(
                icon = Icons.Default.Favorite,
                title = "Premium",
                subtitle = "Quality"
            )
            FeatureItem(
                icon = Icons.Default.Schedule,
                title = "7 Days",
                subtitle = "Return"
            )
            FeatureItem(
                icon = Icons.Default.LocalShipping,
                title = "Free",
                subtitle = "Delivery"
            )
            FeatureItem(
                icon = Icons.Default.Lock,
                title = "Secure",
                subtitle = "Payment"
            )
        }
    }
}

@Composable
private fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun ColorSelectionSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            "Color: Pink",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val colors = listOf(
                androidx.compose.ui.graphics.Color(0xFFE8A5A5),
                androidx.compose.ui.graphics.Color(0xFFC9B8A0),
                androidx.compose.ui.graphics.Color(0xFF000000),
                androidx.compose.ui.graphics.Color(0xFF7A8E8E)
            )

            colors.forEach { color ->
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    color = color
                ) {}
            }
        }
    }
}

@Composable
private fun ProductDetailsSection(
    product: com.berling.marketplace.data.local.entities.ProductEntity,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Product Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val details = listOf(
            "Textured synthetic material for a premium look",
            "Spacious main compartment with inner pockets",
            "Adjustable & detachable sling strap",
            "Zip closure for added security",
            "Perfect for everyday use and special occasions"
        )

        details.forEach { detail ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("•", style = MaterialTheme.typography.bodySmall)
                Text(
                    detail,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        TextButton(onClick = { }) {
            Text("View More")
        }
    }
}

@Composable
private fun SellerInfoCard(
    seller: com.berling.marketplace.data.models.AuthUser,
    sellerRating: Double,
    responseRate: Int,
    followersCount: Int,
    onFollow: () -> Unit,
    onViewShop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsyncImage(
                        model = seller.photoUrl,
                        contentDescription = seller.name,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                seller.name,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (seller.isVerified) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(start = 4.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        AssistChip(
                            onClick = { },
                            label = { Text("Verified Seller") },
                            modifier = Modifier.size(height = 24.dp, width = 100.dp)
                        )
                    }
                }

                Button(onClick = onFollow) {
                    Text("Follow")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn(sellerRating.toString(), "Rating")
                StatColumn("${followersCount / 1000}.${(followersCount % 1000) / 100}K", "Followers")
                StatColumn("248", "Products")
                StatColumn("$responseRate%", "Response Rate")
            }

            Button(
                onClick = onViewShop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.Store,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 4.dp)
                )
                Text("View Seller Shop")
            }
        }
    }
}

@Composable
private fun StatColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun RatingsAndReviewsSection(
    ratings: ProductRatings,
    reviews: List<ReviewData>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Ratings & Reviews",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { }) {
                Text("See All")
            }
        }

        // Overall rating
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "4.7",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "(${ratings.totalReviews} Reviews)",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                RatingBar(5, ratings.fiveStarCount, ratings.totalReviews)
                RatingBar(4, ratings.fourStarCount, ratings.totalReviews)
                RatingBar(3, ratings.threeStarCount, ratings.totalReviews)
                RatingBar(2, ratings.twoStarCount, ratings.totalReviews)
                RatingBar(1, ratings.oneStarCount, ratings.totalReviews)
            }
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp))

        // Reviews
        reviews.forEach { review ->
            ReviewItem(review, modifier = Modifier.padding(bottom = 12.dp))
        }
    }
}

@Composable
private fun RatingBar(stars: Int, count: Int, total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(stars) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        LinearProgressIndicator(
            progress = if (total > 0) count.toFloat() / total else 0f,
            modifier = Modifier
                .weight(1f)
                .height(6.dp),
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            count.toString(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(24.dp)
        )
    }
}

@Composable
private fun ReviewItem(review: ReviewData, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AsyncImage(
                model = review.buyerPhoto,
                contentDescription = review.buyerName,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    review.buyerName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                if (review.verifiedPurchase) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Verified Purchase") },
                        modifier = Modifier.size(height = 20.dp, width = 110.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(5) { index ->
                Icon(
                    if (index < review.rating) Icons.Default.Star else Icons.Default.StarOutline,
                    contentDescription = null,
                    Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "${review.daysAgo} days ago",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Text(
            review.comment,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth()
        )

        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun WriteReviewButton(modifier: Modifier = Modifier) {
    Button(
        onClick = { },
        modifier = modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Icon(
            Icons.Default.RateReview,
            contentDescription = null,
            modifier = Modifier
                .size(16.dp)
                .padding(end = 4.dp)
        )
        Text("Write a Review")
    }
}

@Composable
private fun MoreFromSellerSection(
    products: List<com.berling.marketplace.data.local.entities.ProductEntity>,
    modifier: Modifier = Modifier
) {
    if (products.isNotEmpty()) {
        Column(modifier = modifier) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "More from Seller",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { }) {
                    Text("See Shop")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                products.forEach { product ->
                    MoreFromSellerCard(product)
                }
            }
        }
    }
}

@Composable
private fun MoreFromSellerCard(product: com.berling.marketplace.data.local.entities.ProductEntity) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Column {
            Box {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        )
                        .size(24.dp)
                ) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    product.title,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2
                )
                Text(
                    "₹${product.price}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ActionButtonsSection(
    onMessage: () -> Unit,
    onFollow: () -> Unit,
    onShare: () -> Unit,
    onReportSeller: () -> Unit,
    onBlockSeller: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                icon = Icons.Default.Chat,
                label = "Message",
                onClick = onMessage,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                icon = Icons.Default.PersonAdd,
                label = "Follow",
                onClick = onFollow,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                icon = Icons.Default.Share,
                label = "Share",
                onClick = onShare,
                modifier = Modifier.weight(1f)
            )
        }

        ListItem(
            headlineContent = { Text("Report Seller") },
            leadingContent = { Icon(Icons.Default.Report, contentDescription = null) },
            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        ListItem(
            headlineContent = { Text("Block Seller") },
            leadingContent = { Icon(Icons.Default.Block, contentDescription = null) },
            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(40.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier
                .size(16.dp)
                .padding(end = 4.dp)
        )
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
