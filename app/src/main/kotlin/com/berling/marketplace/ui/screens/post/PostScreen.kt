@file:OptIn(ExperimentalMaterial3Api::class)
package com.berling.marketplace.ui.screens.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.berling.marketplace.ui.screens.BottomNavigationBar
import com.berling.marketplace.ui.screens.UiState

@Composable
fun PostScreen(
    navController: NavHostController,
    viewModel: PostScreenViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val selectedImages by viewModel.selectedImages.collectAsState()
    val postState by viewModel.postState.collectAsState()
    
    var currentStep by remember { mutableStateOf(1) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var showUploadProgress by remember { mutableStateOf(false) }

    LaunchedEffect(postState) {
        if (postState is UiState.Success) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post New Product") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { /* Show preview */ }) {
                        Text("Preview", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController, "post") }
    ) { paddingValues ->
        if (postState is UiState.Loading || showUploadProgress) {
            PostUploadProgressScreen(uploadProgress)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Step indicator
                StepIndicator(currentStep = currentStep, totalSteps = 2)

                when (currentStep) {
                    1 -> {
                        Step1BasicInformation(
                            formState = formState,
                            selectedImages = selectedImages,
                            onFormChange = { field, value -> viewModel.updateFormField(field, value) },
                            onAddImages = { viewModel.addImages(it) },
                            onRemoveImage = { viewModel.removeImage(it) },
                            onLocationPicked = { location ->
                                viewModel.updateFormField("location", location.name)
                                viewModel.updateFormField("latitude", location.latitude)
                                viewModel.updateFormField("longitude", location.longitude)
                                showLocationPicker = false
                            }
                        )
                    }
                    2 -> {
                        Step2DetailsAndOptions(
                            formState = formState,
                            onFormChange = { field, value -> viewModel.updateFormField(field, value) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Navigation buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text("Back")
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep < 2) {
                                currentStep++
                            } else {
                                // Post product
                                showUploadProgress = true
                                viewModel.postProduct("token", "sellerId")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        enabled = isStepValid(currentStep, formState)
                    ) {
                        Text(if (currentStep < 2) "Next" else "Post Listing")
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { step ->
            val stepNum = step + 1
            val isActive = stepNum <= currentStep
            val isCompleted = stepNum < currentStep

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp),
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(2.dp)
            ) {}

            Text(
                "Step $stepNum of $totalSteps",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun Step1BasicInformation(
    formState: PostFormState,
    selectedImages: List<android.graphics.Bitmap>,
    onFormChange: (String, Any) -> Unit,
    onAddImages: (List<android.graphics.Bitmap>) -> Unit,
    onRemoveImage: (Int) -> Unit,
    onLocationPicked: (LocationResult) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Photo Upload Section
        PhotoUploadSection(
            images = selectedImages,
            onAddImages = onAddImages,
            onRemoveImage = onRemoveImage
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("Basic Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        // Product Title
        TextField(
            value = formState.title,
            onValueChange = { onFormChange("title", it) },
            label = { Text("Product Title") },
            placeholder = { Text("e.g. Pink Handbag") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category
        CategoryDropdown(
            selectedCategory = formState.category,
            onCategorySelected = { onFormChange("category", it) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Brand
        TextField(
            value = formState.brand,
            onValueChange = { onFormChange("brand", it) },
            label = { Text("Brand (Optional)") },
            placeholder = { Text("e.g. Lavie") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.LocalOffer, contentDescription = null) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Condition
        ConditionDropdown(
            selectedCondition = formState.condition,
            onConditionSelected = { onFormChange("condition", it) }
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("Price & Stock", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextField(
                value = formState.price.toString().takeIf { it != "0.0" } ?: "",
                onValueChange = { if (it.isEmpty()) onFormChange("price", 0.0) else onFormChange("price", it.toDoubleOrNull() ?: 0.0) },
                label = { Text("Price") },
                placeholder = { Text("₹ 0") },
                modifier = Modifier.weight(1f),
                leadingIcon = { Text("₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            TextField(
                value = formState.discountPrice.toString().takeIf { it != "0.0" } ?: "",
                onValueChange = { if (it.isEmpty()) onFormChange("discountPrice", 0.0) else onFormChange("discountPrice", it.toDoubleOrNull() ?: 0.0) },
                label = { Text("Discount Price (Optional)") },
                placeholder = { Text("₹ 0") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Product Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = formState.description,
            onValueChange = { onFormChange("description", it) },
            label = { Text("Describe your product") },
            placeholder = { Text("Add size, color, material, usage, condition and other details...") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            minLines = 5
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("Product Attributes (Optional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        ProductAttributesSection(
            attributes = formState.productAttributes,
            onAttributeChange = { key, value ->
                val newAttrs = formState.productAttributes.toMutableMap()
                if (value.isEmpty()) newAttrs.remove(key) else newAttrs[key] = value
                onFormChange("productAttributes", newAttrs)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("More Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        // Location
        LocationSearchField(
            location = formState.location,
            onLocationChange = { onFormChange("location", it) },
            onLocationSelected = onLocationPicked,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Delivery Options
        DeliveryOptionsSection(
            selectedOptions = formState.deliveryOptions,
            onOptionToggle = { option ->
                val newOptions = formState.deliveryOptions.toMutableList()
                if (option in newOptions) newOptions.remove(option) else newOptions.add(option)
                onFormChange("deliveryOptions", newOptions)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Return Policy
        ReturnPolicyDropdown(
            selectedPolicy = formState.returnPolicy,
            onPolicySelected = { onFormChange("returnPolicy", it) }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun Step2DetailsAndOptions(
    formState: PostFormState,
    onFormChange: (String, Any) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Product Tags (Optional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        TagInputField(
            tags = formState.tags,
            onTagsChange = { onFormChange("tags", it) }
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("Set a Price", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        PriceTypeCard(
            isFixedPrice = true,
            title = "Fixed Price",
            description = "Set a fixed price for your product",
            isSelected = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        PriceTypeCard(
            isFixedPrice = false,
            title = "Negotiable",
            description = "Allow buyers to make offers",
            isSelected = false
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("More Options", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        // Mark as New
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Mark as New", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("Product is brand new", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = formState.isNew,
                onCheckedChange = { onFormChange("isNew", it) }
            )
        }

        Divider()

        // Boost Listing
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Boost Listing", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("Increase visibility of your product", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = formState.boostListing,
                onCheckedChange = { onFormChange("boostListing", it) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Safe & Secure note
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text("Safe & Secure", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Your listing will be reviewed to ensure a safe marketplace for everyone.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PhotoUploadSection(
    images: List<android.graphics.Bitmap>,
    onAddImages: (List<android.graphics.Bitmap>) -> Unit,
    onRemoveImage: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { /* Open image picker */ },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Add Photos", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                    Text("Add up to 10 photos", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (images.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                images.forEachIndexed { index, bitmap ->
                    Box(modifier = Modifier.size(80.dp)) {
                        AsyncImage(
                            model = bitmap,
                            contentDescription = "Product image $index",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { onRemoveImage(index) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryDropdown(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categories = listOf("Electronics", "Fashion", "Home", "Beauty", "Sports", "Books", "Other")
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            placeholder = { Text("Select category") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ConditionDropdown(selectedCondition: String, onConditionSelected: (String) -> Unit) {
    val conditions = listOf("New", "Like New", "Good", "Fair", "Poor")
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = selectedCondition,
            onValueChange = {},
            readOnly = true,
            label = { Text("Condition") },
            placeholder = { Text("Select condition") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            conditions.forEach { condition ->
                DropdownMenuItem(
                    text = { Text(condition) },
                    onClick = {
                        onConditionSelected(condition.lowercase())
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ProductAttributesSection(
    attributes: Map<String, String>,
    onAttributeChange: (String, String) -> Unit
) {
    val attributeTypes = listOf("Size", "Color", "Material", "Style", "Pattern")

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(attributeTypes) { attr ->
            TextField(
                value = attributes[attr] ?: "",
                onValueChange = { onAttributeChange(attr, it) },
                label = { Text(attr) },
                placeholder = { Text("Enter $attr") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
private fun DeliveryOptionsSection(
    selectedOptions: List<String>,
    onOptionToggle: (String) -> Unit
) {
    val options = listOf("Pickup", "Shipping")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Delivery Options", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))

        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOptionToggle(option) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = option in selectedOptions,
                    onCheckedChange = { onOptionToggle(option) }
                )
                Text(option, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun ReturnPolicyDropdown(selectedPolicy: String, onPolicySelected: (String) -> Unit) {
    val policies = listOf("No Returns", "7 Days", "14 Days", "30 Days")
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = selectedPolicy,
            onValueChange = {},
            readOnly = true,
            label = { Text("Return Policy") },
            placeholder = { Text("Select return policy") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            policies.forEach { policy ->
                DropdownMenuItem(
                    text = { Text(policy) },
                    onClick = {
                        onPolicySelected(policy)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TagInputField(tags: List<String>, onTagsChange: (List<String>) -> Unit) {
    var tagInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = tagInput,
            onValueChange = { tagInput = it },
            label = { Text("Add tags") },
            placeholder = { Text("e.g. trendy, summer, partywear") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                if (tagInput.isNotEmpty()) {
                    IconButton(onClick = {
                        val newTags = tags.toMutableList()
                        newTags.add(tagInput)
                        onTagsChange(newTags)
                        tagInput = ""
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add tag")
                    }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (tags.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { tag ->
                    AssistChip(
                        onClick = { },
                        label = { Text(tag) },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    val newTags = tags.toMutableList()
                                    newTags.remove(tag)
                                    onTagsChange(newTags)
                                },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove tag", modifier = Modifier.size(12.dp))
                            }
                        }
                    )
                }
            }
        }

        Text(
            "Add up to 10 tags",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun PriceTypeCard(isFixedPrice: Boolean, title: String, description: String, isSelected: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            RadioButton(
                selected = isSelected,
                onClick = { }
            )
        }
    }
}

@Composable
private fun PostUploadProgressScreen(uploadProgress: UploadProgress) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (uploadProgress.progress < 100) {
            CircularProgressIndicator(
                progress = uploadProgress.progress / 100f,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "${uploadProgress.progress}%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                uploadProgress.currentFile,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            LinearProgressIndicator(
                progress = uploadProgress.progress / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
            )
        } else {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Product Posted Successfully!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Your product is now live on the marketplace",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun isStepValid(step: Int, formState: PostFormState): Boolean {
    return when (step) {
        1 -> formState.title.isNotEmpty() && formState.category.isNotEmpty() && formState.description.isNotEmpty()
        2 -> formState.price > 0
        else -> false
    }
}
