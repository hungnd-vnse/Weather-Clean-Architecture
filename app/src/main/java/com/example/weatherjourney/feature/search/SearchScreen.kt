package com.example.weatherjourney.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weatherjourney.R
import com.example.weatherjourney.core.designsystem.component.AddressWithFlag
import com.example.weatherjourney.core.designsystem.component.SearchTopBar
import com.example.weatherjourney.core.designsystem.component.SearchTopBarAction
import com.example.weatherjourney.core.model.Location
import com.example.weatherjourney.core.ui.ObserveAsEvents
import com.example.weatherjourney.feature.search.SearchUiEvent.NavigateToDetails

@Composable
fun SearchRoute(
    onBackClick: () -> Unit,
    onLocationClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.uiEvent) { event ->
        when (event) {
            is NavigateToDetails -> onLocationClick(event.locationId)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (searchResults.isEmpty()) {
            Text(
                stringResource(R.string.no_result),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        SearchScreen(
            query = query,
            searchResults = searchResults,
            onBackClick = onBackClick,
            onLocationClick = viewModel::saveLocation,
            onQueryChange = viewModel::onQueryChanged,
        )
    }
}

@Composable
fun SearchScreen(
    query: String,
    searchResults: List<Location>,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onLocationClick: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SearchTopBar(
            query = query,
            onQueryChange = onQueryChange,
            action = SearchTopBarAction.WithBack(onBackClick)
        )
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
            searchResults(
                locations = searchResults,
                onLocationClick = onLocationClick
            )
        }
    }
}

fun LazyListScope.searchResults(
    locations: List<Location>,
    onLocationClick: (Location) -> Unit,
    itemModifier: Modifier = Modifier,
) {
    items(
        items = locations,
        key = { it.id }
    ) { location ->
        Column(
            modifier = itemModifier
                .fillMaxWidth()
                .clickable { onLocationClick(location) }
        ) {
            AddressWithFlag(
                countryCode = location.countryCode,
                address = location.address,
                modifier = Modifier.padding(16.dp)
            )
            HorizontalDivider()
        }
    }
}
