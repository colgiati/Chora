package com.craftworks.music.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.session.MediaController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.craftworks.music.R
import com.craftworks.music.data.Screen
import com.craftworks.music.data.albumList
import com.craftworks.music.data.selectedArtist
import com.craftworks.music.data.songsList
import com.craftworks.music.player.SongHelper
import com.craftworks.music.providers.navidrome.getNavidromeArtistDetails
import com.craftworks.music.shuffleSongs
import com.craftworks.music.ui.elements.AlbumRow
import com.craftworks.music.ui.elements.BottomSpacer
import com.craftworks.music.ui.elements.HorizontalSongCard

@ExperimentalFoundationApi
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ArtistDetails(
    navHostController: NavHostController = rememberNavController(),
    mediaController: MediaController? = null
) {

    val leftPadding = if (LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE) 0.dp else 80.dp
    val imageFadingEdge = Brush.verticalGradient(listOf(Color.Red.copy(0.75f), Color.Transparent))

    LaunchedEffect(selectedArtist) {
        getNavidromeArtistDetails(selectedArtist.navidromeID)
    }

    val artistSongs = songsList.filter { it.artist.contains(selectedArtist.name) }
    val artistAlbums = albumList.filter { it.artist.contains(selectedArtist.name) }
        .sortedByDescending { album -> album.year }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(
            start = leftPadding,
            top = WindowInsets.statusBars
                .asPaddingValues()
                .calculateTopPadding()
        )
        .wrapContentHeight()
        .verticalScroll(rememberScrollState())
    ) {
        Box (modifier = Modifier
            .padding(horizontal = 12.dp)
            .height(192.dp)
            .fillMaxWidth()) {
            //Image and Name
            AsyncImage(
                model = selectedArtist.imageUri,
                placeholder = painterResource(R.drawable.s_a_username),
                fallback = painterResource(R.drawable.s_a_username),
                contentScale = ContentScale.FillWidth,
                contentDescription = "Artist Image",
                modifier = Modifier
                    .fillMaxWidth()
                    //.fadingEdge(imageFadingEdge)
                    .clip(if (selectedArtist.description != "")
                            RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp)
                        else
                            RoundedCornerShape(12.dp))
                    .blur(12.dp)
            )
            Button(
                onClick = {
                    navHostController.popBackStack() },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(top = 12.dp, start = 12.dp)
                    .size(32.dp),
                contentPadding = PaddingValues(2.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    tint = MaterialTheme.colorScheme.onBackground,
                    contentDescription = "Go Back",
                    modifier = Modifier
                        .height(32.dp)
                        .size(32.dp)
                )
            }


            // Album Name and Artist
            Column(modifier = Modifier.align(Alignment.BottomCenter)){
                Text(
                    text = selectedArtist.name,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = 32.sp
                )
            }
        }

        // Description
        var expanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth()
            .heightIn(min = if (selectedArtist.description.isBlank()) 0.dp else 32.dp)
            .clip(RoundedCornerShape(0.dp, 0.dp, 12.dp, 12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .animateContentSize()
            .clickable {
                expanded = !expanded
            }){
            if (selectedArtist.description.isNotBlank()){
                Text(
                    text = selectedArtist.description,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Light,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    textAlign = TextAlign.Start,
                    maxLines = if (expanded) 100 else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(if (selectedArtist.description.isBlank()) 0.dp else 6.dp)
                )
            }
        }

        // Play and shuffle buttons
        Row (modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = {
                    if (artistSongs.isEmpty()) return@Button
                    SongHelper.currentSong = artistSongs[0]
                    SongHelper.currentList = artistSongs
                    artistSongs[0].media?.let { SongHelper.playStream(it, false, mediaController)}
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onBackground),
                modifier = Modifier.width(128.dp)
            ) {
                Row (verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(24.dp)) {
                    Icon(Icons.Rounded.PlayArrow, "Play Album")
                    Text(stringResource(R.string.Action_Play))
                }
            }
            Button(
                onClick = {
                    if (artistSongs.isEmpty()) return@Button
                    shuffleSongs.value = true
                    mediaController?.shuffleModeEnabled = true

                    val random = artistSongs.indices.random()
                    SongHelper.currentSong = artistSongs[random]
                    SongHelper.currentList = artistSongs
                    artistSongs[random].media?.let { SongHelper.playStream(it, false, mediaController)}
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onBackground),
                modifier = Modifier.width(128.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(24.dp)) {
                    Icon(ImageVector.vectorResource(R.drawable.round_shuffle_28), "Shuffle Album")
                    Text(stringResource(R.string.Action_Shuffle))
                }
            }
        }

        /* RECENTLY ADDED SONGS */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(224.dp)
        ) {
            Text(
                text = stringResource(R.string.Screen_Recent_Albums) + ":",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                modifier = Modifier.padding(start = 12.dp)
            )

            /* SONGS ROW */
            AlbumRow(albums = artistAlbums, mediaController, onAlbumSelected = { album ->
                navHostController.navigate(Screen.AlbumDetails.route) {
                    launchSingleTop = true
                }
                selectedAlbum = album }
            )
        }

        // Songs List
        Column(modifier = Modifier
            .padding(start = 12.dp, end = 12.dp, top = 0.dp)
            .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.Screen_Top_Songs) + ":",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                for(song in artistSongs){
                    HorizontalSongCard(song = song, onClick = {
                        SongHelper.currentSong = song
                        SongHelper.currentList = artistSongs
                        song.media?.let { SongHelper.playStream(it, false, mediaController)}
                    })
                }
            }
        }

        BottomSpacer()
    }
}