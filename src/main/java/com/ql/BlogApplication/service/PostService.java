package com.ql.BlogApplication.service;

import com.ql.BlogApplication.constant.MessageCodes;
import com.ql.BlogApplication.dto.ApiResponse;
import com.ql.BlogApplication.dto.CommentResponseDto;
import com.ql.BlogApplication.dto.PostRequestDto;
import com.ql.BlogApplication.dto.PostResponseDto;
import com.ql.BlogApplication.entity.*;
import com.ql.BlogApplication.exception.CategoryNotFoundException;
import com.ql.BlogApplication.exception.PostNotFoundException;
import com.ql.BlogApplication.exception.UserNotFoundException;
import com.ql.BlogApplication.mapper.CommentMapper;
import com.ql.BlogApplication.mapper.PostMapper;
import com.ql.BlogApplication.repository.CategoryRepository;
import com.ql.BlogApplication.repository.PostRepository;
import com.ql.BlogApplication.repository.UserRepository;
import com.ql.BlogApplication.util.JwtUtil;
import com.ql.BlogApplication.util.TokenContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.regions.Region;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;


@Service
public class PostService {

      private final PostRepository postRepository;
      private final UserRepository userRepository;
      private final CategoryRepository categoryRepository;
      private final JwtUtil jwtUtil;
      private final S3Client s3Client;
      private final S3Presigner s3Presigner;
      private static final Logger logger = LoggerFactory.getLogger(PostService.class);

      @Value("${aws.s3.bucket}")
       private String bucketName;

      @Value(("${aws.region}"))
      private String awsRegion;


    PostService(S3Presigner s3Presigner,S3Client s3Client,PostRepository postRepository, UserRepository userRepository, CategoryRepository categoryRepository, JwtUtil jwtUtil){
          this.postRepository=postRepository;
          this.userRepository=userRepository;
          this.categoryRepository=categoryRepository;
          this.jwtUtil=jwtUtil;
          this.s3Client=s3Client;
          this.s3Presigner=s3Presigner;
      }

       //working fine getting all post
       public ResponseEntity<ApiResponse<List<PostResponseDto>>> getAllPosts(){

          List<Post> allPosts=postRepository.findByIsPublishedTrue();
          List<PostResponseDto> postResponseDtoList=PostMapper.toDtoList(allPosts);

          ApiResponse<List<PostResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),postResponseDtoList,"Posts fetched successfully.");
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);

      }

       //working fine check for same post
       public ResponseEntity<ApiResponse<String>> createPost(PostRequestDto postRequestDto) {

          Category category=categoryRepository.findById(postRequestDto.getCategoryId()).orElseThrow(()-> new CategoryNotFoundException(MessageCodes.messages.get(231)));

          Post newPost=new Post();
          newPost.setTitle(postRequestDto.getTitle());
          newPost.setContent(postRequestDto.getContent());
          newPost.setImageURL(postRequestDto.getImageUrl());

          String token= TokenContext.getToken();
          Long id= Long.parseLong(jwtUtil.extractId(token));
          User user=userRepository.findById(id).orElseThrow(()-> new UserNotFoundException(MessageCodes.messages.get(201)));
          newPost.setAuthor(user);
          newPost.setCategory(category);
          postRepository.save(newPost);

          ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),MessageCodes.messages.get(121),MessageCodes.messages.get(121));
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

       //uploading image in s3 bucket
       public ResponseEntity<ApiResponse<String>> uploadImage(Long id,MultipartFile multipartFile) {

        try{

            String token= TokenContext.getToken();
            Long userId= Long.parseLong(jwtUtil.extractId(token));

            String fileName= UUID.randomUUID()+"_"+multipartFile.getOriginalFilename();
            Post post=postRepository.findByAuthorIdAndId(userId,id).orElseThrow(()-> new PostNotFoundException(MessageCodes.messages.get(221)));

            if(post.getImageURL()!=null){
               String oldKey=post.getImageURL().substring(post.getImageURL().lastIndexOf("/")+1);
               s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(oldKey).build());
            }

            PutObjectRequest putObjectRequest=PutObjectRequest.builder()
                            .bucket(bucketName)
                                    .key(fileName)
                                            .contentType(multipartFile.getContentType())
                                                    .acl(ObjectCannedACL.PUBLIC_READ)
                                                            .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(multipartFile.getInputStream(),multipartFile.getSize()));
            String imageUrl="https://" + bucketName + ".s3.amazonaws.com/" + fileName;
            post.setImageURL(imageUrl);

            postRepository.save(post);
            logger.info("Image saved successfully");

        }catch (IOException ioException){
            logger.error("error while uploading image on s3");
            ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Internal server error","internal server error");
            return new ResponseEntity<>(apiResponse,HttpStatus.INTERNAL_SERVER_ERROR);
        }

          ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),"image uploaded successfully","image uploaded successfully");
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

      //generating presigned url for uploading image
       public ResponseEntity<ApiResponse<String>> generatePreSignedUrl(String fileName,String contentType){
           try{
               String uniqueFilename = UUID.randomUUID() + "_" + fileName;
               PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                       .bucket(bucketName)
                       .key(uniqueFilename)
                       .contentType(contentType)
                       .acl(ObjectCannedACL.PUBLIC_READ)
                       .build();

               PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                       .signatureDuration(Duration.ofMinutes(15))
                       .putObjectRequest(putObjectRequest)
                       .build();

               PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

               ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(), presignedRequest.url().toString(), "PreSigned URL generated successfully");
               return new ResponseEntity<>(apiResponse,HttpStatus.OK);
           }catch (Exception e) {
               e.printStackTrace();
               ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.OK.value(), null, "Failed to generate PreSigned URL");
               return new ResponseEntity<>(apiResponse,HttpStatus.OK);
           }
       }

       //confirmed upload from presigned url and save to db
       public ResponseEntity<ApiResponse<String>> confirmImageUpload(Long postId,String imageUrl) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(MessageCodes.messages.get(221)));

        if(post.getImageURL()!=null){
               String oldKey=post.getImageURL().substring(post.getImageURL().lastIndexOf("/")+1);
               s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(oldKey).build());
        }

        post.setImageURL(imageUrl);
        postRepository.save(post);

        ApiResponse<String> apiResponse=ApiResponse.<String>success(HttpStatus.OK.value(), null, "Image URL saved successfully");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

       //publish a post if not published
       public ResponseEntity<ApiResponse<String>> publishPost(Long id){

        String token= TokenContext.getToken();
        Long userId= Long.parseLong(jwtUtil.extractId(token));

        Post post=postRepository.findByAuthorIdAndId(userId,id).orElseThrow(()-> new PostNotFoundException(MessageCodes.messages.get(221)));

        if(post.getIsPublished()==Boolean.TRUE){
            post.setIsPublished(false);
            postRepository.save(post);
            ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),"Post unpublish successfully", "Post unpublish successfully");
            return new ResponseEntity<>(apiResponse,HttpStatus.OK);
        }

        post.setIsPublished(true);
        postRepository.save(post);

        ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(),"Post published successfully", "Post published successfully");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

       //working fine getting all post under a category
       public ResponseEntity<ApiResponse<List<PostResponseDto>>> findAllPostByCategory(String category){

            List<Post> allPosts=postRepository.findByIsPublishedTrue();

            List<Post> filterPosts= allPosts.stream().filter(post->post.getCategory().getName().equals(category)).toList();

            List<PostResponseDto>  postResponseDtoList = PostMapper.toDtoList(filterPosts);

            ApiResponse<List<PostResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),postResponseDtoList,"All posts inside category");
            return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

       //working fine getting all comment list under a post
       public ResponseEntity<ApiResponse<List<CommentResponseDto>>> findAllCommentByPostId(Long id){

          String token= TokenContext.getToken();
          Long userId= Long.parseLong(jwtUtil.extractId(token));

          Post post=postRepository.findByAuthorIdAndId(userId,id).orElseThrow(()->new PostNotFoundException(MessageCodes.messages.get(221)));

          List<Comment> comments=post.getComments();

          List<CommentResponseDto> allComments= CommentMapper.toDtoList(comments);

          ApiResponse<List<CommentResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),allComments, "All comments for a post");
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

}
