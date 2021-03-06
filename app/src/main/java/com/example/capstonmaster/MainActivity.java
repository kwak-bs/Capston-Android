package com.example.capstonmaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.capstonmaster.Util.PreferenceUtil;
import com.example.capstonmaster.board.advice_board.Advicefragment;
import com.example.capstonmaster.board.department_board.Departmentfragment;
import com.example.capstonmaster.board.free_board.Freefragment;
import com.example.capstonmaster.board.promote_board.PromoteFragment;
import com.example.capstonmaster.board.used_board.UsedFragment;
import com.example.capstonmaster.dto.Author;
import com.example.capstonmaster.home.HomePageAdapter;
import com.example.capstonmaster.history.HistoryPagerAdapter;
import com.example.capstonmaster.message.MessageActivity;
import com.example.capstonmaster.metoring.MentoPageAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;
    public ViewPager viewPager;
    public TabLayout tabLayout;
    private SectionsPageAdapter sectionsPageAdapter;
    private HomePageAdapter homePageAdapter;
    private MentoPageAdapter mentoPageAdapter;
    private HistoryPagerAdapter historyPagerAdapter;
    SharedPreferences sf;
    String userToken;
    private int REQUEST_TEST = 1;
    TextView h_email;
    TextView h_nickname;
//    ImageButton imagebutton;
    String id;
    String[] category;
    FragmentManager fragmentManager;
    View headerView;
    NavigationView navigationView;
    public static String userName;
    String email;
    String src;
    Author account;
    ImageButton profileImg;
    TabLayout.TabLayoutOnPageChangeListener tabLayoutOnPageChangeListener;
    TabLayout.OnTabSelectedListener onTabSelectedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        userToken= PreferenceUtil.getInstance(this).getStringExtra("userToken");
        System.out.println(userToken + "토큰 있음 ㅇㅇ");


        getBoardList();
        int c = 0;
        while (category==null) {
            try {
                c++;
                Thread.sleep(500);
                if (c % 5==0) {
                    getBoardList();
                }
                if(c==10){
                    PreferenceUtil.getInstance(getApplicationContext()).removePreference("userToken");
                    System.out.println("토큰지움");
                    Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                System.out.println("category 대기중");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.InitializeLayout();
        getAccount();
        while (userName==null ||email==null) {
            try {
                c++;
                Thread.sleep(500);
                if (c % 5==0) {
                    getAccount();
                }
                System.out.println("userName 대기중");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        h_email.setText(email);
        h_nickname.setText(userName);

        navigationView = findViewById(R.id.nav_view);
        profileImg.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Mypage.class);
                startActivity(intent);
            }
        });
        Glide.with(this).load(src).override(150,100).error(R.drawable.circle).into(profileImg);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbar_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Toast.makeText(getApplicationContext(), "새로고침", Toast.LENGTH_SHORT).show();
//                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//                ft.detach(sectionsPageAdapter.getItem(1)).attach(sectionsPageAdapter.getItem(1));
//                ft.commit();
                this.InitializeLayout();
                viewPager.setCurrentItem(tabLayout.getSelectedTabPosition());
                return true;
            case R.id.action_message:
                Intent intent=new Intent(this, MessageActivity.class);
                startActivity(intent);
//                Toast.makeText(getApplicationContext(), " 메세지", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void InitializeLayout() {

        //toolBar를 통해 App Bar 생성
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//      toolbar.setTitleTextColor(Color.parseColor("#B0BEC5"));
//      toolbar.setBackgroundColor(Color.parseColor("#ffffff"));

        fragmentManager = getSupportFragmentManager();
        homePageAdapter = new HomePageAdapter(fragmentManager);
        sectionsPageAdapter = new SectionsPageAdapter(fragmentManager);
        mentoPageAdapter = new MentoPageAdapter(fragmentManager);
        historyPagerAdapter = new HistoryPagerAdapter(fragmentManager);
        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(homePageAdapter);
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setVisibility(View.GONE);
        if (category == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < category.length; i++) {
            tabLayout.addTab(tabLayout.newTab().setText(category[i]));
        }
//        tabLayoutOnPageChangeListener = new TabLayout.TabLayoutOnPageChangeListener(tabLayout);


        //App Bar의 좌측 영영에 Drawer를 Open 하기 위한 Incon 추가
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//    getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher_round);

        DrawerLayout drawLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        headerView = navigationView.getHeaderView(0);
        profileImg= headerView.findViewById(R.id.imageProfile);
        h_email = headerView.findViewById(R.id.h_email);
        h_nickname = headerView.findViewById(R.id.h_nickname);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawLayout,
                toolbar,
                R.string.app_name,
                R.string.app_name
        );

        drawLayout.addDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle.syncState();

//    //프래그먼트 장착
//    mAppBarConfiguration = new AppBarConfiguration.Builder(
//      R.id.login,R.id.free,R.id.notice)
//      .setDrawerLayout(drawLayout)
//      .build();
//    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//
////         ActionBar에도 Top-Level-Destination 설정 하면,
////Top-Level-Destincation이 아니라면 Actionbar에 뒤로 가기[<-] 가 표현된다
//    NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
//    NavigationUI.setupWithNavController(navigationView, navController);

//    navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
//
//      public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
//
//        Toast.makeText(MainActivity.this,destination.getLabel(),Toast.LENGTH_LONG).show();
//      }
//    });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.home:
                        Toast.makeText(getApplicationContext(), "홈", Toast.LENGTH_SHORT).show();
                        viewPager.clearOnPageChangeListeners();
                        viewPager.setAdapter(homePageAdapter);
                        tabLayout.setVisibility(View.GONE);
                        break;
                    case R.id.history:
                        Toast.makeText(getApplicationContext(), "학과연혁", Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(MainActivity.this, HistoryFragment.class);
//                        startActivity(intent);
                      viewPager.clearOnPageChangeListeners();
                      viewPager.setAdapter(historyPagerAdapter);
                      tabLayout.setVisibility(View.GONE);
                        break;
                  case R.id.free:
                    Toast.makeText(getApplicationContext(), "자유", Toast.LENGTH_SHORT).show();
                    boardPageInit();
//                        tabLayout.setVisibility(View.VISIBLE);
//                        viewPager.setAdapter(sectionsPageAdapter);
//                        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
//                        viewPager.setCurrentItem(0);
                    break;

                    case R.id.mento_menti:
                        Toast.makeText(getApplicationContext(), "멘토", Toast.LENGTH_SHORT).show();
//                        fragmentManager.beginTransaction().replace(R.id.viewpager,new MentoFragment()).commit();
                        viewPager.clearOnPageChangeListeners();
                        viewPager.setAdapter(mentoPageAdapter);
                        tabLayout.setVisibility(View.GONE);
//                        viewPager.setOffscreenPageLimit(0);
//                        viewPager.removeOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
                        break;
                    case R.id.logout:
                        PreferenceUtil.getInstance(getApplicationContext()).removePreference("userToken");
                        System.out.println("토큰지움");
                        Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        break;

                }
//                getSupportActionBar().setTitle();
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    private void boardPageInit() {
        tabLayout.setVisibility(View.VISIBLE);
        viewPager.setAdapter(sectionsPageAdapter);
//        for (int i = 0; i < category.length; i++) {
//            tabLayout.addTab(tabLayout.newTab().setText(category[i]));
//        }
//    tabLayout.setupWithViewPager(viewPager);

//        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("메인일시정지");
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("메인재개");
    }

    @Override
    protected void onRestart() {
        System.out.println("메인재시작");
        super.onRestart();
    }

    @Override
    protected void onStop() {
        System.out.println("메인스탑");
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    protected void onDestroy() {
//        SharedPreferences.Editor editor = sf.edit();
//        editor.remove("pref");
//        editor.commit();
//        System.out.println("토큰지움");
        PreferenceUtil.getInstance(getApplicationContext()).removePreference("userToken");
        System.out.println("토큰지움");
        super.onDestroy();
    }

    public void getBoardList() {

        OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .header(getString(R.string.Authorization), "Bearer " + userToken)
                .url(getString(R.string.ip) + "/api/article/category")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println(e);
                System.out.println("메인액티비티카테고리 가져오기 실패");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    category = new String[jsonArray.length()];
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonobject = jsonArray.getJSONObject(i);
                        id = jsonobject.getString("id");
                        category[i] = jsonobject.getString("name");
                        switch (id) {
                            case "1":
                                Departmentfragment.id=id;
                                break;
                            case "2":
                                Freefragment.id = id;
                                break;
                            case "3":
                                Advicefragment.id=id;
                                break;
                            case "4":
                                UsedFragment.id=id;
                                break;
                            case "5":
                                PromoteFragment.id=id;
                                break;
                        }
                        System.out.println(id + " " + category[i]);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        });
    }

    public void getAccount() {
        //System.out.println("=======" + access_token);
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .header(getString(R.string.Authorization), "Bearer " + userToken)
                .url(getString(R.string.ip) + "/api/accounts/login")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println(e);
                System.out.println("getAccount 실패");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    Gson gson = new Gson();
                    String a =response.body().string();
                    System.out.println(a+"뭐라도찍어봐ㅠㅠ");
                    account = gson.fromJson(a, Author.class);
                    userName=account.getName();
                    email=account.getEmail();
                    src=account.getImgSrc();
                    System.out.println(src+" ~~~src ");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
