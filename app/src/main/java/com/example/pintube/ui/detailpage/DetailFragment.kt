package com.example.pintube.ui.detailpage

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.pintube.MainActivity
import com.example.pintube.data.remote.api.retrofit.YouTubeApi
import com.example.pintube.data.remote.api.retrofit.YoutubeSearchService
import com.example.pintube.data.remote.dto.ApiResponse
import com.example.pintube.data.repository.ApiRepositoryImpl
import com.example.pintube.databinding.FragmentDetailBinding
import com.example.pintube.domain.entitiy.VideoEntity
import com.example.pintube.domain.repository.ApiRepository
import com.example.pintube.ui.detailpage.comment.CommentAdapter
import com.example.pintube.ui.home.HomeFragment
import com.example.pintube.ui.home.HomeViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class DetailFragment : Fragment() {

    private var binding : FragmentDetailBinding? = null

    private var tempMediaId: String = "rkuE-ygaSgQ"
    private lateinit var mediaItemData: DetailItemModel

    private lateinit var mContext: Context
//    private lateinit var commentAdapter: CommentAdapter

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession

    private var videoSample =
        "\"embedHtml\": \"\\u003ciframe width=\\\"480\\\" height=\\\"270\\\" src=\\\"//www.youtube.com/embed/kutvmV17kKg\\\" frameborder=\\\"0\\\" allow=\\\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\\\" allowfullscreen\\u003e\\u003c/iframe\\u003e\""

    private val viewModel: DetailViewModel by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        binding!!.ivDetailClose.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding!!.ivDetailShare.setOnClickListener {
            shareLink()
        }
        binding!!.ivDetailPin.setOnClickListener {
            //보관함 저장
        }
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initPlayer()
        initSession()

    }

    override fun onStart() {
        super.onStart()
        player.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        player.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
        binding = null
    }

    private fun initView() {
        //뷰 초기화, 받아온 정보 배치
    }

    private fun initPlayer() {
        val mediaItem = MediaItem.fromUri(videoSample)

        player = ExoPlayer.Builder(requireContext())
            .build()
            .also { exoPlayer ->
                binding!!.playerDetail.player = exoPlayer
            }
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        player.addListener(object : Player.Listener{
            override fun onPlayerError(error: PlaybackException) {
                Snackbar.make(binding!!.detailFragment, "영상을 재생하는 중 문제가 발생했습니다.", Snackbar.LENGTH_SHORT).show()
                Log.d("player", "error = ${error}")
                super.onPlayerError(error)
            }
        })

    }

    private fun initSession() {
        mediaSession = MediaSession.Builder(requireContext(), player)
            .setSessionActivity(
                PendingIntent.getActivity(
                    requireContext(),
                    0,
                    Intent(requireContext(), MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
    }

    private suspend fun getData(id: String?) {
//        mediaId = //id값 받아와서 그 값으로 검색?해서 해당 영상 정보 가져오기
        YouTubeApi.youtubeNetwork.getContentDetails(ids = listOf(tempMediaId))
        ApiRepositoryImpl().getContentDetails(listOf(tempMediaId))?.forEach {
            videoSample = it.player.toString()
        }
        val detailData = mediaItemData.player
    }

    private fun shareLink() {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "link")
            type = "text/plain"
        }
        val shareChooser = Intent.createChooser(intent, null)
        startActivity(shareChooser)
    }

}