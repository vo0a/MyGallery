package e.darom.mygallery

/*
사진을 이미지 뷰에 표시하기

imageView.setImageUIR(Uri.parse("/storage/emulated/0/DCIM/Camera/IMG_20180605_144124.jpg")) 처럼 이미지 뷰에 표시할 수 있지만
이미지를 표시할 때는 Glide 라이브러리를 사용하는 방법을 추천합니다.Glide 라이브러리는 미사용 리소스를 자동으로 해제하고 메모리를 효율적으로 관리해주기 때문입니다.
그리고 이미지를 비동기로 로딩하여 UI의 끊김이 없습니다.

1.  클래스 선언 밖에 const 키워드를 사용하여 상수를 정의하면 컴파일 시간에 결정되는 상수가 되고 이 파일 내에서 어디서든 사용할 수 있습니다.
컴파일 시간 상수의 초기화는 String 또는 프리미티브형(Int, Long, Double 등 기본형)으로만 초기화할 수 있습니다.

2. newInstance() 메서드를 이용하여 프래그먼트를 생성할 수 있고 인자로 uri값을 전달합니다.
이 값은 Bundle 객체에 ARG_URI 키로 저장되고 arguments 프로퍼티에 저장됩니다.

3. 프래그먼트가 생성되면 onCreate() 메서드가 호출되고 ARG_URI 키에 저장된 uri 값을 얻어서 변수에 저장합니다.

4. onCreateView() 메서드에서는 프래그먼트에 표시될 뷰를 생성합니다. 액티비티가 아닌 곳에서 레이아웃 리소스를 가지고 오려면
LayoutInflater 객체의 inflate() 메서드를 사용합니다. R.layout.fragment_photo 레이아웃 파일을 가지고 와서 반환합니다.
기본 코드를 그대로 두면 됩니다.

이제 뷰가 완성된 직후에는 호출되는 onViewCreated() 메서드를 오버라이드하고 Glide 라이브러리로 사진을 이미지 뷰에 표시하겠습니다.

5. Glide.with(this) 로 사용 준비를 하고 load() 메서드에 uri 값을 인자로 주고 해당 이미지를 부드럽게 로딩합니다.
이미지가 로딩되면 into() 메서드로 imageView에 표시합니다.

이미지를 빠르고 부드럽게 로딩하고 메모리 관리까지 자동으로 하고 싶다면 Glide를 사용하세요. 코드는 같은 한 줄이지만 성능이 매우 향상됩니다.

 */

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_photo.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

private const val ARG_URI = "uri" //1.

/**
 * A simple [Fragment] subclass.
 * Use the [PhotoFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class PhotoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var uri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //3.
        arguments?.let {
            uri = it.getString(ARG_URI)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // 4.
        return inflater.inflate(R.layout.fragment_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(this).load(uri).into(imageView) //   5.
    }

    //2.
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PhotoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(uri: String) =
            PhotoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_URI, uri)
                }
            }
    }
}
