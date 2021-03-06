package com.hutquan.hut.controller;

import com.hutquan.hut.pojo.Dynamic;
import com.hutquan.hut.pojo.User;
import com.hutquan.hut.service.IWithFriendsService;
import com.hutquan.hut.utils.RedisUtils;
import com.hutquan.hut.vo.PageBean;
import com.hutquan.hut.vo.ResponseBean;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;


/**
 * 发布查看动态信息模块
 *
 */
@RestController
public class WithFriendsHomeController {

    @Autowired
    private IWithFriendsService iWithFriendsService;

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 发布动态
     * @param dynamic
     * @return
     */
    @PostMapping("/withfriend/releasedc")
    @ApiOperation("发布动态 ->图片为非必要上传项 这里因为服务器负载的原因不接入地址数据")
    public  ResponseBean releaseDynamic(Dynamic dynamic, @RequestParam(value = "file",required = false) MultipartFile[] files, HttpServletRequest request){
        User user = (User) redisUtils.get(request.getHeader("token"));
        if(user == null) return new ResponseBean(401,"未登录",null);
        //User user = (User) request.getSession().getAttribute("user");
        try {
            return new ResponseBean(200,"ok",iWithFriendsService.addDynamic(user,dynamic,files));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(500, "未知错误", null);
        }

    }

    /**
     * 按热度查询动态排序

     * @return
     */
    @GetMapping("/withfriend/dynamic")
    @ApiOperation("热点动态，点赞量最多的20条动态")
    public ResponseBean Dynamic(HttpServletRequest request){
        User user = (User) redisUtils.get(request.getHeader("token"));
        //TODO 应该把热点动态的相关数据直接缓存到Redis，定时刷新
        return new ResponseBean(200,"ok",iWithFriendsService.dynamicsByHot(user));

    }

    /**
     * 按时间顺序
     * @param pageNum
     * @param request
     * @return
     */
    @GetMapping("/withfriend/dynamicbytime/{pageNum}")
    @ApiOperation("按照时间由近到远排序动态")
    public ResponseBean DynamicByTime(@PathVariable("pageNum") int pageNum,HttpServletRequest request){
        try{
            User user = (User) redisUtils.get(request.getHeader("token"));
            PageBean<Dynamic> dynamicPageInfo = iWithFriendsService.dynamicsByTime(pageNum,6,user);

            return new ResponseBean(200, "ok", dynamicPageInfo);

        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500,"未知错误",null);
        }
    }


    /**
     * 关注的人的动态
     * @param pageNum
     * @param request
     * @return
     */
    @GetMapping("/withfriend/condynamic/{pageNum}")
    @ApiOperation("关注的人的动态")
    public ResponseBean ConDynamic(@PathVariable("pageNum") int pageNum, HttpServletRequest request){
        User user = (User) redisUtils.get(request.getHeader("token"));
        if(user == null){
            return new ResponseBean(403,"未登录",null);
        }
        try {
            PageBean<Dynamic> dynamicPageInfo = iWithFriendsService.condynamic(pageNum, 6, user);
            if(dynamicPageInfo != null) {
                return new ResponseBean(200, "ok", dynamicPageInfo);
            }else {
                return new ResponseBean(200,"ok",null); //没有关注的人
            }
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500,"未知错误",null);
        }
    }

    @GetMapping("/withfriend/querydyamic/{userId}/{pageNum}")
    @ApiOperation("查询他人的动态,可以不登陆查看,userId指的是被查看动态的用户的用户Id")
    public ResponseBean queryDynamic(@PathVariable("userId") int userId,@PathVariable("pageNum") int pageNum, HttpServletRequest request){
        User user = (User) redisUtils.get(request.getHeader("token"));
        //TODO 必要的话可以添加黑名单权限相关的东西
        try {
            PageBean<Dynamic> dynamicPageInfo = iWithFriendsService.queryDynamic(userId,pageNum, 6, user);

            return new ResponseBean(200, "ok", dynamicPageInfo);

        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(500,"未知错误",null);
        }
    }


    /**
     * 添加关注
     * @param concernUserId
     * @param request
     * @return
     */
    @GetMapping("/withfriend/addconcern")
    @ApiOperation("添加关注")
    public ResponseBean Concern(@RequestParam int concernUserId, HttpServletRequest request){
        //User user = (User) request.getSession().getAttribute("user");
        User user = (User) redisUtils.get(request.getHeader("token"));
        if(user == null){
            return new ResponseBean(403,"未登录",null);
        }
        if(iWithFriendsService.addConcern(user,concernUserId)){
            return new ResponseBean(200,"ok",null);
        }else {
            return new ResponseBean(-1,"fail",null);
        }
    }

    /**
     * 取消关注
     * @param concernUserId
     * @param request
     * @return
     */
    @GetMapping("/withfriend/remconcern")
    @ApiOperation("取消关注")
    public ResponseBean remConcern(@RequestParam int concernUserId, HttpServletRequest request){
        //User user = (User) request.getSession().getAttribute("user");
        User user = (User) redisUtils.get(request.getHeader("token"));
        if(user == null){
            return new ResponseBean(403,"未登录",null);
        }
        if(iWithFriendsService.remConcern(user,concernUserId)){
            return new ResponseBean(200,"ok",null);
        }else {
            return new ResponseBean(-1,"fail",null);
        }
    }

    /**
     * 添加喜欢
     * @param dynamicId
     * @param request
     * @return
     */
    @GetMapping("/withfriend/like/{dynamicId}")
    @ApiOperation("对动态添加喜欢,返回值为该动态最新的点赞量")
    public ResponseBean likeDynamic(@PathVariable("dynamicId") int dynamicId,HttpServletRequest request){
        //User user = (User) request.getSession().getAttribute("user");
        User user = (User) redisUtils.get(request.getHeader("token"));
        if(user == null){
            return new ResponseBean(403,"未登录",null);
        }
        return new ResponseBean(200,"ok",
                iWithFriendsService.likeDynamic(user,dynamicId));
    }

    /**
     * 取消喜欢
     * @param dynamicId
     * @param request
     * @return
     */
    @GetMapping("/withfriend/cancellike/{dynamicId}")
    @ApiOperation("取消对动态添加的喜欢,返回值为该动态最新的点赞量")
    public ResponseBean cancellikeDynamic(@PathVariable("dynamicId") int dynamicId,HttpServletRequest request){
        //User user = (User) request.getSession().getAttribute("user");
        User user = (User) redisUtils.get(request.getHeader("token"));
        if(user == null){
            return new ResponseBean(403,"未登录",null);
        }
        return new ResponseBean(200,"ok",
                iWithFriendsService.cancellikeDynamic(user,dynamicId));
    }

    @PostMapping("/withfriend/delDynamic/{dynamicId}")
    @ApiOperation("删除自己的动态")
    public ResponseBean delDynamic(@PathVariable("dynamicId")int dynamicId,HttpServletRequest request){
        User user = (User) redisUtils.get(request.getHeader("token"));
        if(user == null){
            return new ResponseBean(403,"未登录",null);
        }
        return new ResponseBean(200,"ok",iWithFriendsService.delDynamic(dynamicId,user));
    }


}
