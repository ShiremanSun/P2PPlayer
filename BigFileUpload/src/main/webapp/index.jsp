
<%@page import="com.itheima.ck.bean.MovieDao"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ include file="head.jsp" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="utf-8"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <link rel="stylesheet" href="bootstrap-3.3.7-dist/css/bootstrap.css"/>
    <link rel="stylesheet" type="text/css" href="webuploader-0.1.5/webuploader.css">
    <script type="text/javascript" src="jquery-1.11.3/jquery.js"></script>
    <script type="text/javascript" src="bootstrap-3.3.7-dist/js/bootstrap.js"></script>

    <script type="text/javascript" src="webuploader-0.1.5/webuploader.js"></script>

    <style type="text/css">
        .bottom-20px {
            margin-top: 20px;
            margin-bottom: 20px;
        }
    </style>

</head>
<body>


<a href = "">查看电影</a>

<div class="container">

  <div style="margin-top:50px">
  	<textarea id="name" placeholder="请输入电影名称"></textarea>
  </div>
  <div style="margin-top:50px">
  	<textarea id="details"  style="width:300px;height:100px" placeholder="请输入电影描述"></textarea>
  </div>
  <div class="row bottom-20px" >
  	<div >
  		<div id="thelist" class="uploader-list"></div>
  		<div id="picker">选择电影</div>
  	</div>
  	<div style="margin-top:30px">
  		<div id="imagelist" class="uploader-list"></div>
  		<div id="filePicker">选择图片</div>
  	</div>
  	
  </div>
   
    <div class="row bottom-20px">
        <button id="ctlBtn" class="btn btn-default">开始上传</button>
    </div>
</div>

<%
  //创建数据库
  MovieDao.getInstance().CreateTable();
%>

<script type="text/javascript">
    var $ = jQuery,
        $list = $('#thelist'),
        $imagelist = $('#imagelist'),
        $btn = $('#ctlBtn'),
        ratio = window.devicePixelRatio || 1,
        thumbnailWidth = 100 * ratio,
        thumbnailHeight = 100 * ratio,
        state = 'pending';

    var name;
    var details;
   
    //电影文件上传
    var uploader = WebUploader.create({

        // swf文件路径
        swf: '${ctx}/webuploader-0.1.5/Uploader.swf',

        // 文件接收服务端。
        server: '${ctx}/upload.do',
        //文件上传请求的参数表，每次发送都会发送此对象中的参数
        formData: {
            md5: ''
        },

        // 选择文件的按钮。可选。
        // 内部根据当前运行是创建，可能是input元素，也可能是flash.
        pick: '#picker',
        

        // 不压缩image, 默认如果是jpeg，文件上传前会压缩一把再上传！
        resize: false,

        chunked: true, // 分块
        chunkSize: 1 * 1024 * 1024, // 字节 1M分块
        threads: 10, //开启线程
        auto: false,

        // 禁掉全局的拖拽功能。这样不会出现图片拖进页面的时候，把图片打开。
        disableGlobalDnd: true,
        //单文件上传
        fileNumLimit: 1,
        fileSizeLimit: 10000 * 1024 * 1024,    // 10 G
        fileSingleSizeLimit: 10000 * 1024 * 1024    // 10 M
    });

    // 当有文件被添加进队列的时候
    uploader.on('fileQueued', function (file) {
        console.log("文件队列事件被触发..");
        $list.append('<div id="' + file.id + '" class="item">' +
            '<h4 class="info">' + file.name + '</h4>' +
            '<p class="state"></p>' +
            '</div>');

        var _file = $("#" + file.id);
        uploader.md5File( file )
        // 及时显示进度
            .progress(function(percentage) {
                //console.log('Percentage:', percentage);
                _file.find("p").html("准备中:"+ percentage * 100 + "%");
            })

            // 完成
            .then(function(val) {
                uploader.options.formData.md5 = val;
                _file.find("p").html("准备完成,等待上传.");
            });

    });

     uploader.on('uploadBeforeSend', function (obj, data) {
        //传入表单参数
        data = $.extend(data, {
            "moviename": $('#name').val(),
            "details": $('#details').val()
        });
    }); 
   
    // 文件上传过程中创建进度条实时显示。
    uploader.on('uploadProgress', function (file, percentage) {
        var $li = $('#' + file.id),
            $percent = $li.find('.progress .progress-bar');

        // 避免重复创建
        if (!$percent.length) {
            $percent = $('<div class="progress progress-striped active">' +
                '<div class="progress-bar" role="progressbar" style="width: 0%">' +
                '</div>' +
                '</div>').appendTo($li).find('.progress-bar');
        }

        $li.find('p.state').text('上传中');

        $percent.css('width', percentage * 100 + '%');
    });

    uploader.on('uploadSuccess', function (file) {
        $('#' + file.id).find('p.state').text('已上传');
    });

    uploader.on('uploadError', function (file) {
        $('#' + file.id).find('p.state').text('上传出错');
    });

    uploader.on('uploadComplete', function (file) {
        $('#' + file.id).find('.progress').fadeOut();
    });
    
  //电影封面上传
    var uploader2 = WebUploader.create({
    	// swf文件路径
        swf: '${ctx}/webuploader-0.1.5/Uploader.swf',
        server: '${ctx}/imageUpload',
        pick: '#filePicker',
        fileNumLimit: 1,
     // 只允许选择图片文件。
        accept: {
            title: 'Images',
            extensions: 'gif,jpg,jpeg,bmp,png',
            mimeTypes: 'image/*'
        }
        
    });
    
    
 // 当有文件添加进来的时候
    uploader2.on( 'fileQueued', function( file ) {
        var $li = $(
                '<div id="' + file.id + '" class="file-item thumbnail">' +
                    '<img>' +
                    '<div class="info">' + file.name + '</div>' +
                '</div>'
                ),
            $img = $li.find('img');


        // $list为容器jQuery实例
        $imagelist.append( $li );

        // 创建缩略图
        // 如果为非图片文件，可以不用调用此方法。
        // thumbnailWidth x thumbnailHeight 为 100 x 100
        uploader.makeThumb( file, function( error, src ) {
            if ( error ) {
                $img.replaceWith('<span>不能预览</span>');
                return;
            }

            $img.attr( 'src', src );
        }, thumbnailWidth, thumbnailHeight );
    });
    
    uploader2.on( 'uploadProgress', function( file, percentage ) {
        var $li = $( '#'+file.id ),
            $percent = $li.find('.progress span');

        // 避免重复创建
        if ( !$percent.length ) {
            $percent = $('<p class="progress"><span></span></p>')
                    .appendTo( $li )
                    .find('span');
        }

        $percent.css( 'width', percentage * 100 + '%' );
    });
    
    uploader2.on('uploadBeforeSend', function (obj, data) {
        //传入表单参数
        data = $.extend(data, {
            "moviename": $('#name').val(),
            "details": $('#details').val()
        });
    });
 // 文件上传成功，给item添加成功class, 用样式标记上传成功。
    uploader2.on( 'uploadSuccess', function( file ) {
        $( '#'+file.id ).addClass('upload-state-done');
    });

    // 文件上传失败，显示上传出错。
    uploader2.on( 'uploadError', function( file ) {
        var $li = $( '#'+file.id ),
            $error = $li.find('div.error');

        // 避免重复创建
        if ( !$error.length ) {
            $error = $('<div class="error"></div>').appendTo( $li );
        }

        $error.text('上传失败');
    });

    // 完成上传完了，成功或者失败，先删除进度条。
    uploader2.on( 'uploadComplete', function( file ) {
        $( '#'+file.id ).find('.progress').remove();
    });
    


    $btn.on('click', function () {
    	name=$('#name').val();
    	details=$('#details').val();

    	if(!name || !details) {
    		alert("请输入完整内容");
    	} else {
    	//查询数据库是否已经有该文件，如果有，再提示
    	
    	if (state === 'uploading') {
    	        alert("正在上传");
    	    } else {
    	    	uploader2.upload();
    	        uploader.upload();
    	  }
    	}
       
    });

</script>
</body>
</html>