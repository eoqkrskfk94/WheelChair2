package com.example.mjkim.wheelchair2.Login;


import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mjkim.wheelchair2.MenuScreen;
import com.example.mjkim.wheelchair2.NameSearch.FirebaseJson;
import com.example.mjkim.wheelchair2.R;
import com.example.mjkim.wheelchair2.Review.ReviewJson;
import com.example.mjkim.wheelchair2.Review.ReviewList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import com.google.firebase.storage.StorageReference;

// 내가 쓴 리뷰보는 activity.
public class MyReview extends AppCompatActivity {
    public static ArrayList<ReviewJson> reviewJson = new ArrayList<ReviewJson>();
    FirebaseAuth auth;
    private DatabaseReference mDatabase;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    public int reviewcount=0;
    StorageReference ref;
    FirebaseJson fullJson;
    private static String json;
    ReviewList get;
    String total;
    ReviewList reviewList;


    public static ArrayList<ReviewList> reviewLists;
    TextView address_name, phone_number, review_count;
    private UserReviewAdapter reviewAdapter;
    private String location_name;
    private String key;
    private String name;
    private String review_name, reviewer_name;
    private int mapx, mapy,  num;
    static public int totalLocationCount;
    private double rating;
    private Boolean tag1;
    private Boolean tag2;
    private Boolean tag3;
    private Boolean tag4;
    private Boolean tag5;
    private Boolean tag6;
    private String review;
    private String imageUrl1;
    private String imageUrl2;
    private String imageUrl3;
    private String imageUrl4;
    private String imageUrl5;
    private String imageUrl6;
    private String imageUrl7;
    private String imageUrl8;
    private String imageUrl9;
    private String date;
    private String phone = "";
    private String address = "";
    private String email = "";

    final Context context;

    {
        context = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_review);
        totalLocationCount = 0;
        reviewLists = new ArrayList<ReviewList>();


        // 내 리뷰 띄우기
        auth = FirebaseAuth.getInstance();
        final String userEmail = new String(auth.getCurrentUser().getEmail()); //Useremail이 현재 사용자 이메일이다.
        mDatabase = database.getReference();
        final DatabaseReference mymyreview = database.getReference();

        Toast.makeText(MyReview.this, "로딩중입니다",
                Toast.LENGTH_SHORT).show();


        mDatabase.child("review lists").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


                mymyreview.child("review lists").child(dataSnapshot.getKey()).orderByChild("email").equalTo(userEmail).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        ReviewList myreview = dataSnapshot.getValue(ReviewList.class);
                        key=dataSnapshot.getKey();
                        System.out.println("삭제삭제"+key);
                        location_name = myreview.getLocation_name();
                         //myreview.getLocation_name();
                        System.out.println("yeeeeeee1 : " + location_name);
//                        name = intent.getExtras().getString("NAME");
//                        address_name.setText(intent.getExtras().getString("ROAD_ADDRESS"));
//                        address = intent.getExtras().getString("ROAD_ADDRESS");
//                        phone_number.setText(intent.getExtras().getString("TELEPHONE"));
//                        phone = intent.getExtras().getString("TELEPHONE");
//                        mapx = intent.getExtras().getInt("MAPX");
//                        mapy = intent.getExtras().getInt("MAPY");
                        date = myreview.getDate();
                        review_name = myreview.getReview_name();
                        rating = myreview.getRating();
                        tag1 = myreview.getTag1();
                        tag2 = myreview.getTag2();
                        tag3 = myreview.getTag3();
                        tag4 = myreview.getTag4();
                        tag5 = myreview.getTag5();
                        tag6 = myreview.getTag6();
                        review = myreview.getReview();
                        System.out.println("yeeeeeee3 : " + review);
                        email = myreview.getEmail();
                        imageUrl1 = myreview.getImageUrl1();
                        imageUrl2 = myreview.getImageUrl2();
                        imageUrl3 = myreview.getImageUrl3();
                        imageUrl4 = myreview.getImageUrl4();
                        imageUrl5 = myreview.getImageUrl5();
                        imageUrl6 = myreview.getImageUrl6();
                        imageUrl7 = myreview.getImageUrl7();
                        imageUrl8 = myreview.getImageUrl8();
                        imageUrl9 = myreview.getImageUrl9();

                        reviewList = new ReviewList();

                        reviewList.setKey(key);
                        reviewList.setReview_name(review_name);
                        reviewList.setLocation_name(location_name);
                        reviewList.setRating(rating);
                        reviewList.setTag1(tag1);
                        reviewList.setTag2(tag2);
                        reviewList.setTag3(tag3);
                        reviewList.setTag4(tag4);
                        reviewList.setTag5(tag5);
                        reviewList.setTag6(tag6);
                        reviewList.setReview(review);
                        reviewList.setDate(date);
                        reviewList.setImageUrl1(imageUrl1);
                        reviewList.setImageUrl2(imageUrl2);
                        reviewList.setImageUrl3(imageUrl3);
                        reviewList.setImageUrl4(imageUrl4);
                        reviewList.setImageUrl5(imageUrl5);
                        reviewList.setImageUrl6(imageUrl6);
                        reviewList.setImageUrl7(imageUrl7);
                        reviewList.setImageUrl8(imageUrl8);
                        reviewList.setImageUrl9(imageUrl9);


                        //System.out.println("reviewList : " + reviewList.getLocation_name());
                        reviewList.setLocation_name(location_name);
                        reviewLists.add(totalLocationCount, reviewList);

                        totalLocationCount++;

                        // 리뷰 갯수
                        review_count = (TextView) findViewById(R.id.review_count);
                        review_count.setText(Integer.toString(totalLocationCount));



                        System.out.println(dataSnapshot.getKey() + " was " + myreview.getEmail() +myreview.getDate()+"\n"+myreview.getReview()+ " meters tall.");
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                        ReviewList myreview = dataSnapshot.getValue(ReviewList.class);
//                        System.out.println(dataSnapshot.getKey() + " was " + myreview.getEmail() +myreview.getDate()+ " meters tall.");
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//                        ReviewList myreview = dataSnapshot.getValue(ReviewList.class);
//                        System.out.println(dataSnapshot.getKey() + " was " + myreview.getEmail() +myreview.getDate()+ " meters tall.");
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                        ReviewList myreview = dataSnapshot.getValue(ReviewList.class);
//                        System.out.println(dataSnapshot.getKey() + " was " + myreview.getEmail() +myreview.getDate()+ " meters tall.");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        System.out.println("errororororororor");
                    }
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        // 리뷰 어댑터 끼우기
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ListView reviewListView = (ListView) findViewById(R.id.my_review_list);
                reviewAdapter = new UserReviewAdapter(MyReview.this, reviewLists,context,MyReview.this,totalLocationCount);
                reviewListView.setAdapter(reviewAdapter);



            }
        }, 2300);








        ImageButton back_button;
        ImageButton menu_button;

        back_button = (ImageButton)findViewById(R.id.back_b);
        menu_button = (ImageButton)findViewById(R.id.menu_b);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        menu_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){ openMenuTab(); }});




    }


    public void openMenuTab(){
        Intent intent = new Intent(this, MenuScreen.class);
        startActivity(intent);
    }

}
