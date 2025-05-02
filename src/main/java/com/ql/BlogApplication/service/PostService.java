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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.*;


@Service
public class PostService {

      private final PostRepository postRepository;
      private final UserRepository userRepository;
      private final CategoryRepository categoryRepository;
      private final JwtUtil jwtUtil;
      private final S3Client s3Client;
      private final S3Presigner s3Presigner;
      private final CommentMapper commentMapper;
      private final PostMapper postMapper;
      private  final Logger logger = LoggerFactory.getLogger(PostService.class);

      @Value("${aws.s3.bucket}")
       private String bucketName;

      @Value(("${aws.region}"))
      private String awsRegion;

      @Value("${file.uploads-dir}")
      private String uploadDir;


    PostService(PostMapper postMapper,CommentMapper commentMapper,S3Presigner s3Presigner,S3Client s3Client,PostRepository postRepository, UserRepository userRepository, CategoryRepository categoryRepository, JwtUtil jwtUtil){
          this.postRepository=postRepository;
          this.userRepository=userRepository;
          this.categoryRepository=categoryRepository;
          this.jwtUtil=jwtUtil;
          this.s3Client=s3Client;
          this.s3Presigner=s3Presigner;
          this.commentMapper=commentMapper;
          this.postMapper=postMapper;
    }

       //working fine getting all post
       public ResponseEntity<ApiResponse< Map<String,List<PostResponseDto>> >> getAllPosts(){

          List<Post> allPosts=postRepository.findByIsPublishedTrue();
          List<PostResponseDto> postResponseDtoList=postMapper.toDtoList(allPosts);

           Map<String,List<PostResponseDto>> data=new HashMap<>();
           data.put("All posts",postResponseDtoList);

          ApiResponse< Map<String,List<PostResponseDto>> > apiResponse=ApiResponse.success(HttpStatus.OK.value(),data,"Posts fetched successfully.");
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);

      }

       //working fine check for same post
       public ResponseEntity<ApiResponse< Map<String,PostResponseDto> >> createPost(PostRequestDto postRequestDto) {

          Category category=categoryRepository.findById(postRequestDto.getCategoryId()).orElseThrow(()-> new CategoryNotFoundException(MessageCodes.messages.get(231)));

          Post post=new Post();
          post.setTitle(postRequestDto.getTitle());
          post.setContent(postRequestDto.getContent());

          String token= TokenContext.getToken();
          Long userId= Long.parseLong(jwtUtil.extractId(token));
          User user=userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException(MessageCodes.messages.get(201)));
          post.setAuthor(user);
          post.setCategory(category);
          postRepository.save(post);

          PostResponseDto postResponseDto=postMapper.toDto(post);

           Map<String,PostResponseDto> data=new HashMap<>();
           data.put("Post Created",postResponseDto);

          ApiResponse<Map<String,PostResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),data,MessageCodes.messages.get(121));
          return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

       //publish a post if not published
       public ResponseEntity<ApiResponse<Map<String,PostResponseDto>>> publishPost(Long postId){

        String token= TokenContext.getToken();
        Long userId= Long.parseLong(jwtUtil.extractId(token));

        Post post=postRepository.findByAuthorIdAndId(userId,postId).orElseThrow(()-> new PostNotFoundException(MessageCodes.messages.get(221)));
        PostResponseDto postResponseDto=postMapper.toDto(post);
        Map<String,PostResponseDto> data=new HashMap<>();

        if(post.getIsPublished()==Boolean.TRUE){
            post.setIsPublished(false);
            postRepository.save(post);
            data.put("Unpublished_post",postResponseDto);
            ApiResponse<Map<String,PostResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),data, "Post unpublish successfully");
            return new ResponseEntity<>(apiResponse,HttpStatus.OK);
        }

        post.setIsPublished(true);
        postRepository.save(post);
        data.put("Published_post",postResponseDto);
        ApiResponse<Map<String,PostResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),data, "Post published successfully");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

      //working fine getting all post under a category
       public ResponseEntity<ApiResponse<Map<String,List<PostResponseDto>>>> findAllPostByCategory(String category){

        List<Post> allPosts=postRepository.findByIsPublishedTrue();

        List<Post> filterPosts= allPosts.stream().filter(post->post.getCategory().getName().equals(category)).toList();
        List<PostResponseDto>  postResponseDtoList = postMapper.toDtoList(filterPosts);

        Map<String,List<PostResponseDto>> data=new HashMap<>();
        data.put("All_post_by_category",postResponseDtoList);

        ApiResponse< Map<String,List<PostResponseDto>>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),data,"All posts inside category");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

       //working fine getting all comment list under a post
       public ResponseEntity<ApiResponse<Map<String,List<CommentResponseDto>>>> findAllCommentByPostId(Long postId){

        Post post=postRepository.findById(postId).orElseThrow(()->new PostNotFoundException(MessageCodes.messages.get(221)));

        if(post.getIsPublished()==Boolean.FALSE){
               ApiResponse<Map<String,List<CommentResponseDto>>> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(),null,"Unpublished post");
               return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
        }

        List<Comment> comments=post.getComments();

        List<CommentResponseDto> commentResponseDtoList= commentMapper.toDtoList(comments);

        Map<String,List<CommentResponseDto>> data=new HashMap<>();
        data.put("Comment_on_post",commentResponseDtoList);

        ApiResponse<Map<String,List<CommentResponseDto>>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),data, "All comments for a post");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

      //working fine upload image locally
       public ResponseEntity<ApiResponse<String>> uploadImageOnLocal(Long id,MultipartFile multipartFile) {

           try{
               String contentType = multipartFile.getContentType();
               logger.info("Content type :{}",contentType);
               if (contentType == null || !contentType.startsWith("image/")) {
                   ApiResponse<String> apiResponse = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), null, "Only image files are allowed");
                   return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
               }

               String token= TokenContext.getToken();
               Long userId= Long.parseLong(jwtUtil.extractId(token));

               String fileName= UUID.randomUUID()+"_"+multipartFile.getOriginalFilename();
               Post post=postRepository.findByAuthorIdAndId(userId,id).orElseThrow(()-> new PostNotFoundException(MessageCodes.messages.get(221)));

               if(post.getIsPublished()==Boolean.FALSE){
                   ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(),null,"Unpublished post");
                   return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
               }

               Path uploadPath= Paths.get(uploadDir);

               if(!Files.exists(uploadPath)){
                   Files.createDirectories(uploadPath);
               }

               Path filePath=uploadPath.resolve(fileName);
               Files.copy(multipartFile.getInputStream(),filePath, StandardCopyOption.REPLACE_EXISTING);

               String imageUrl="http://localhost:8080/uploads/"+fileName;

               logger.info("URL for the image: {}", imageUrl);

               post.setImageURL(imageUrl);
               postRepository.save(post);
               logger.info("Image saved successfully");

           }catch (IOException ioException){
               ApiResponse<String> apiResponse=ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),null,"internal server error");
               return new ResponseEntity<>(apiResponse,HttpStatus.INTERNAL_SERVER_ERROR);
           }

           ApiResponse<String> apiResponse=ApiResponse.success(HttpStatus.OK.value(), "","image uploaded successfully");
           return new ResponseEntity<>(apiResponse,HttpStatus.OK);

       }

       //uploading image in s3 bucket
       public ResponseEntity<ApiResponse<Map<String,PostResponseDto>>> uploadImageS3(Long id,MultipartFile multipartFile) {

        try{
            String contentType = multipartFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                ApiResponse<Map<String,PostResponseDto>> apiResponse = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), null, "Only image files are allowed");
                return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
            }

            String token= TokenContext.getToken();
            Long userId= Long.parseLong(jwtUtil.extractId(token));

            String fileName= UUID.randomUUID()+"_"+multipartFile.getOriginalFilename();
            Post post=postRepository.findByAuthorIdAndId(userId,id).orElseThrow(()-> new PostNotFoundException(MessageCodes.messages.get(221)));

            if(post.getIsPublished()==Boolean.FALSE){
                ApiResponse<Map<String,PostResponseDto>> apiResponse=ApiResponse.error(HttpStatus.BAD_REQUEST.value(),null,"Unpublished post");
                return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
            }

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

            PostResponseDto postResponseDto=postMapper.toDto(post);

            Map<String,PostResponseDto> data=new HashMap<>();
            data.put("Post",postResponseDto);

            ApiResponse<Map<String,PostResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),data,"image uploaded successfully");
            return new ResponseEntity<>(apiResponse,HttpStatus.OK);
        }catch (IOException ioException){
            logger.error("error while uploading image on s3");
            ApiResponse<Map<String,PostResponseDto>> apiResponse=ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),null,"internal server error");
            return new ResponseEntity<>(apiResponse,HttpStatus.INTERNAL_SERVER_ERROR);
        }

      }

       //generating presigned url for uploading image
       public ResponseEntity<ApiResponse<Map<String,String>>> generatePreSignedUrl(String fileName,String contentType){
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

               Map<String,String> data=new HashMap<>();
               data.put("Pre_signed_url",presignedRequest.url().toString());

               ApiResponse<Map<String,String>> apiResponse=ApiResponse.success(HttpStatus.OK.value(),data, "PreSigned URL generated successfully");
               return new ResponseEntity<>(apiResponse,HttpStatus.OK);
           }catch (Exception e) {
               ApiResponse<Map<String,String>> apiResponse=ApiResponse.error(HttpStatus.OK.value(), null, "Failed to generate PreSigned URL");
               return new ResponseEntity<>(apiResponse,HttpStatus.OK);
           }
       }

       //confirmed upload from presigned url and save to db
       public ResponseEntity<ApiResponse<Map<String,PostResponseDto>>> confirmImageUpload(Long postId,String imageUrl) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(MessageCodes.messages.get(221)));

        if(post.getImageURL()!=null){
               String oldKey=post.getImageURL().substring(post.getImageURL().lastIndexOf("/")+1);
               s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(oldKey).build());
        }

        post.setImageURL(imageUrl);
        postRepository.save(post);

        PostResponseDto postResponseDto=postMapper.toDto(post);

        Map<String,PostResponseDto> data=new HashMap<>();
        data.put("Image_uploaded_on_post",postResponseDto);

        ApiResponse<Map<String,PostResponseDto>> apiResponse=ApiResponse.success(HttpStatus.OK.value(), data, "Image URL saved successfully");
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
      }

}
