package e.darom.mygallery

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import kotlin.concurrent.timer

/*
콘텐츠 프로바이더란 앱의 데이터 접근을 다른 앱에 허용하는 컴포넌트입니다.

프로바이더를 이용하여 사진 정보를 가지고 오는 순서는 크게 다음과 같습니다.
1. 사진 데이터는 외부 저장소에 저장되어 있으므로 외부 저장소 읽기 권한을 앱에 부여합니다.
2. 외부 저장소 읽기 권한은 위험 권한으로 실행 중에 사용자에게 권한을 허용하도록 합니다.
3. contentResolver 객체를 이용하여 데이터를 Cursor 객체로 가지고 옵니다.

위 순서를 기억해두고 다음 과정을 진행합니다.

1. 프로바이더로 기기의 사진 경로얻기
2. 매니페스트에 외부 저장소 읽기 권한 추가
3. 권한 확인
4. 권한 요청
5. 사용 권한 요청 응답 처리
6. 앱 실행
 */

/*
안드로이드 4대 컴포넌트

안드로이드에는 크게 4개의 중요 컴포넌트가 있습니다. 이를 4대 컴포넌트라고 지칭합니다.
- 액티비티 : 화면을 구성합니다.
- 콘텐츠 프로바이더 : 데이터베이스, 파일, 네트워크의 데이터를 다른 앱에 공유합니다.
- 브로드캐스트 리시버 : 앱이나 기기가 발송하는 방송을 수신합니다. 이 책에서는 다루지 않습니다.
- 서비스 : 화면이 없고 백그라운드 작업에 용이합니다.
 */
/*
안드로이드 저장소

안드로이드 저장소는 크게 두 가지 영역으로 나뉩니다.
- 내부 저장소 : OS가 설치된 영역으로 유저가 접근할 수 없는 시스템 영역입니다. 앱이 사용하는 정보와 데이터베이스가 저장됩니다.
- 외부 저장소 : 컴퓨터에 기기를 연결하면 저장소로 인식되며 유저가 사용하는 영역입니다. 사진과 동영상은 외부 저장소에 저장됩니다.
 */
class MainActivity : AppCompatActivity() {

    private val REQUEST_READ_EXTERNAL_STORAGE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //1. 권한이 부여되었는지 확인
        /*
        ContextCompat.checkSelfPermission () 메서드를 사용하여 권한이 있는지 확인합니다.
        앱에 권한이 있으면 PERMISSION_GRANTED 가 반환되고, 없다면 PERMISSION_DENIED 가 반환됩니다.
        이때 사용자에게 명시적으로 권한을 요청해야 합니다.

        Manifest 클래스는 여러 패키지에 존재하는데 코드 작성 중 어느 것을 임포트할지 물어보면 android를 임포트합니다.
        만약 실수로 잘못된 패키지를 선택한 경우에는 해당 임포트문을 삭제하고 다시 진행합니다.

        shouldShowRequestPermissionRationale () 메서드는 사용자가 전에 권한 요청을 거부했는지를 반환합니다. ture를 바환하면 거부를 한 적이 있는 겁니다.

        requestPermissions () 메서드를 사용하여 외부 저장소 읽기 권한을 요청합니다. 마지막 인자인 리퀘스트 코드에는 적당한 정수값을 넣습니다.
        이 값은 권한 요청에 대한 결과를 분기 처리하는 데 사용합니다.
         */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            //  권한이 허용되지 않음 : 이전에 거부했는지 아닌지에 나눠 권한 요청.
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                //  이전에 이미 권한이 거부되을 때 설명
                alert("사진 정보를 얻으려면 외부 저장소 권한이 필수로 필요합니다", "권한이 필요한 이유"){
                    yesButton {
                        //  권한 요청
                        ActivityCompat.requestPermissions(this@MainActivity,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            REQUEST_READ_EXTERNAL_STORAGE)
                    }
                    noButton {  }
                }.show()
            } else {
                //  권한 요청
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_EXTERNAL_STORAGE
                )
            }
        } else {
            //  권한이 이미 허용됨
            getAllPhotos()
        }
    }

    //기기의 사진 경로 얻기
    /*
    프로바이더를 사용해 사진 정보를 얻으려면 contentResolver 객체를 사용해 데이터를 얻을 수 있습니다.
    다음은 외부 저장소에 저장된 모든 사진을 최신 순으로 정렬하여 Cursor라는 객체를 얻는 코드입니다.

    contentResolver 객체의 query() 메서드는 인자 5개를 받습니다.

    1. 첫 번째 인자는 어떤 데이터를 가져오느냐를 URI 형태로 지정합니다. 사진 정보는 외부 저장소에 저장되어 있기 때문에
    외부 저장소에 저장된 데이터를 가리키는 URI인 EXTERNAL_CONTENT_URI 를 지정합니다.

    2. 두 번째 인자는 어떤 항목의 데이터를 가져올 것인지 String 배열로 지정합니다. 가져올 데이터의 구조를 잘 모른다면 일반적으로 null을 지정합니다.
    null을 지정하면 모든 항목을 가져옵니다.

    3. 세 번째 인자는 데이터를 가져올 조건을 지정할 수 있습니다. 전체 데이터를 가져올 때는 null을 설정합니다.

    4. 네 번쨰 인자는 세 번째 인자와 조합하여 조건을 지정할 때 사용합니다. 사용하지 않는다면 null을 설정합니다.

    5. 정렬 방법을 지정합니다. 사진이 찍힌 날짜의 내림차순 정렬을 합니다.
    더 복잡한 데이터 요청도 할 수 있습니다만 더 깊은 SQL 문법 지식이 필요합니다.
     */
    private fun getAllPhotos() {
        //  모든 사진 정보 가져오기
        val cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,    //1.
            null,       //2. 가져올 항목 배열
            null,       //3. 조건
            null,   //4. 조건
            MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC") //5. 찍은 날짜 내림차순

        /*
        1. 사진 정보를 담고 있는 Cursor 객체는 내부적으로 데이터를 이동하는 포인터를 가지고 있어서 moveToNext() 메서드로 다음 정보로 이동하고
        그 결과를 true 로 반환합니다. while 문을 사용하면 모든 데이터를 순회할 수 있습니다. 만약 사진이 없다면 Cursor 객체는 null입니다.

        2. 사진의 경로가 저장된 데이터베이스의 컬럼명은 DATA 상수에 정의되어 있습니다.
        getColumnIndexOfThrow() 메서드를 사용하면 해당 컬럼이 몇 번째 인덱스인지 알 수 있습니다. getString() 메서드에 그 인덱스를 전달하면
        해당하는 데이터를 String 타입으로 반환합니다. 이것이 uri 즉 사진이 저장된 위치의 경로가 됩니다.

        3. Cursor 객체를 더 이상 사용하지 않을 때는 close() 메서드로 닫아야 합니다. 만약 닫지 않으면 메모리 누수가 발생합니다.
        (메모리가 해제되지 않는 상황이 지속되는 것을 메모리 누수라고 말하고 메모리 누수가 쌓이면 잘 동작하던 폰이 느려지고 앱이 죽을 수 있습니다.)

        Logcat 탭을 클릭하고 MainActivity 태그를 필터링하여 사진의 URI가 표시되면 성공입니다. (기기에 사진이 한 장도 없다면 아무것도 표시되지 않습니다.)
         */

        // 1. 사진 정보 로그로 표시
        val fragments = ArrayList<Fragment>()
        if(cursor != null){
            while (cursor.moveToNext()){
                // 2. 사진 경로 Uri 가져오기
                val uri = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                 Log.d("MainActivity", uri)

                fragments.add(PhotoFragment.newInstance(uri))

            }
            cursor.close() // 3.
        }

        // 어댑터
        /*
        프래그먼트를 아이템으로 하는 ArrayList를 생성합니다. 사진을 Cursor 객체로부터 가져올 때마다
        PhotoFragment.newInstance(uri) 로 프래그먼트를 생성하면서 fragments 리스트에 추가합니다.

        MyPagerAdapter 를 생성하면서 프래그먼트 매니저를 생성자의 인자로 전달해야 합니다.
        프래그먼트 매니저는 getSupportFragmentManager() 메서드로 가져올 수 있고 코틀린에서는 supportFragmentManager 프로퍼티로 접근할 수 있습니다.
        어댑터를 생성하면 updateFragments() 메서드를 사용하여 프래그먼트 리스트를 전달합니다. 어댑터를 viewPager 에 설정합니다.

        앱을 실행합니다. 사진이 표시되고 좌우로 슬라이드했을 때 다음 사진으로 넘어가면 성공입니다.
         */
        val adapter = MyPagerAdapter(supportFragmentManager)
        adapter.updateFragment(fragments)
        viewPager.adapter = adapter


        // 슬라이드쇼 구현하기 : 3초마다 자동으로 슬라이드되는 기능 추가(6장 스톱워치의 timer)
        /*
        1. timer로 3초마다 코드 실행하기
        2. runOnUiThread 로 timer 내부에서 UI 조작하기

        1. 3초마다 실행되는 타이머를 생성합니다. 3초마다 실행될 내용은 페이지를 전환하는 UI 변경입니다.
        timer 가 백그라운드 스레드로 동작해 UI를 변경하도록 2. runOnUiThread로 코드를 감쌉니다.

        3. 현재 페이지가 마지막 페이지가 아니라면 4. 다음 페이지로 변경하고, 마지막 페이지라면 첫 페이지로 변경합니다.

        앱을 실행하여 3초마다 페이지가 자동으로 변환되고 마지막 페이지에서 다시 첫 페이지로 이동된다면 성공입니다.
         */
        timer(period = 3000){   // 1.
            runOnUiThread{      // 2.
                if(viewPager.currentItem < adapter.count -1){           //3.
                    viewPager.currentItem = viewPager.currentItem + 1   //4.
                } else{
                    viewPager.currentItem = 0   //5.
                }
            }
        }
    }

    /*
    매니페스트에 외부 저장소 읽기 권한 추가
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

    안드로이드 6.0(API 23)부터 모든 앱은 외부에서 리소스 또는 정보를 사용하는 경우 앱에서 사용자에게 권한을 요청해야 합니다.
    매니페스트에 권한을 나열하고 앱을 실행 중에 사용자에게 각 권한을 승인받으면 됩니다.

    안드로이드 시스템에 의해서 권한은 '정상' 권한과 '위험' 권한으로 분류됩니다. 위험 권한은 실행 중에 사용자에게 권한을 요청해야 합니다.

    다음은 자주 쓰는 위험 권한 중 일부입니다.
    STORAGE - READ_EXTERNAL_STORAGE
            - WRITE_EXTERNAL_STORAGE
    LOCATION - ACCESS_FINE_LOCATION
             - ACCESS_COARSE_LOCATION
    SMS  - SEND_SMS
         - RECEIVE_SMS
    CAMERA - CAMERA
     */

    //  사용 권한 요청 응답 처리
    /*
    권한이 부여되었는지 확인하려면 이 메서드(onRequestPermissionsResult)를 오버라이드해야 합니다.
    응답 결과를 확인하여 사진 정보를 가져오거나 권한이 거부됐다는 토스트 메시지를 표시하는 코드를 다음과 같이 작성합니다.
    자동 완성 기능을 사용하면 편리합니다.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode){
            REQUEST_READ_EXTERNAL_STORAGE -> {
                if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    // 권한 허용됨
                    getAllPhotos()
                } else {
                    // 권한 거부
                    toast("권한 거부 됨")
                }
                return
            }
        }
    }

}
