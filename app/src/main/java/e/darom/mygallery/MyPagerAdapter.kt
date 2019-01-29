package e.darom.mygallery

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

/*
액티비티에 ViewPager 추가
뷰페이저는 여러 프래그먼트들을 좌우로 슬라이드하는 뷰입니다. 뷰 페이저를 사용하려면 데이터, 어댑터, 뷰 세 가지가 필요한 어댑터 패턴을 구현해야 합니다.

- 데이터 : 프래그먼트(화면)
- 어댑터 : 프래그먼트를 어느 화면에 표시할 것인지 관리하는 객체
- 뷰 : 뷰페이저

ViewPager 에 표시할 내용을 정의하려면 어댑터가 필요합니다. 어댑터는 아이템의 목록 정보를 가진 객체입니다.
1. FragmentStatePagerAdapter 클래스를 상속받습니다. + 생성자 파라미터 추가
2. 클래스 이름에서 implement member 클릭으로 미구현된 멤버를 자동 생성합니다.
 */

class MyPagerAdapter(fm: FragmentManager?) : FragmentStatePagerAdapter(fm) {
    // 뷰페이저가 표시할 프래그먼트 목록 : 어댑터가 프래그먼트의 목록을 리스트로 가지도록 합니다. update Fragments() 메서드를 사용해 외부에서 추가할 수 있습니다.
    private val items = ArrayList<Fragment>()

    // position 위치의 프래그먼트 : getItem() 메서드에는 position 위치에 어떤 프래그먼트를 표시할지를 정의해 줍니다.
    override fun getItem(position: Int): Fragment {
        return items[position]
    }
    // 아이템 개수 : getCount() 메서드에는 아이템(프래그먼트) 개수를 정의해줍니다.
    override fun getCount(): Int {
        return items.size
    }
    // 아이템 갱신
    fun updateFragment(items : List<Fragment> ){
        this.items.addAll(items)
    }

}