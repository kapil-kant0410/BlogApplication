package com.ql.BlogApplication.constant;

import java.util.HashMap;
import java.util.Map;

public class MessageCodes {

    public static final Map<Integer,String> messages=new HashMap<>();

    static{

        // SUCCESS MESSAGES (100–199)

        // User (101–110)
        messages.put(101, "User registered successfully");
        messages.put(102, "User logged in successfully");
        messages.put(103, "User updated successfully");
        messages.put(104, "User deleted successfully");
        messages.put(105, "User fetched successfully");
        messages.put(106,"User logged out successfully");

        // Role (111–120)
        messages.put(111, "Role assigned successfully");

        // Post (121–130)
        messages.put(121, "Post created successfully");
        messages.put(122, "Post updated successfully");
        messages.put(123, "Post deleted successfully");
        messages.put(124, "Post fetched successfully");

        // Category (131–140)
        messages.put(131, "Category created successfully");
        messages.put(132, "Category updated successfully");
        messages.put(133, "Category deleted successfully");
        messages.put(134, "Categories fetched successfully");

        // Comment (141–150)
        messages.put(141, "Comment added successfully");
        messages.put(142, "Comment updated successfully");
        messages.put(143, "Comment deleted successfully");
        messages.put(144, "Comment fetched successfully");

        // Like (151–160)
        messages.put(151, "Post liked successfully");
        messages.put(152, "Like removed successfully");
        messages.put(153, "Like fetched successfully");
        messages.put(154, "Comment liked successfully");

        // AuthorSubscription (161–170)
        messages.put(161, "Author subscribed successfully");
        messages.put(162, "Author unsubscribed successfully");
        messages.put(163, "Subscription list fetched successfully");
        messages.put(164,"Subscribers list fetched successfully");

        // UserRole (171–180)
        messages.put(171, "UserRole created successfully");
        messages.put(172, "UserRole updated successfully");
        messages.put(173, "UserRole deleted successfully");
        messages.put(174, "UserRole fetched successfully");

        //  ERROR MESSAGES (200–299)

        // User (201–210)
        messages.put(201, "User not found");

        // Role (211–220)
        messages.put(211, "Role not found");

        // Post (221–230)
        messages.put(221, "Post not found");

        // Category (231–240)
        messages.put(231, "Category not found");

        // Comment (241–250)
        messages.put(241, "Comment not found");

        // Like (251–260)
        messages.put(251, "Like not found");

        // AuthorSubscription (261–270)
        messages.put(261, "Subscription not found");

        // UserRole (271–280)
        messages.put(271, "UserRole not found");

    }


}
