# backend-fullstack-learning

[TOC]

【谷粒商城】

- https://www.bilibili.com/video/BV1np4y1C7Yf/?p=311&spm_id_from=pageDriver&vd_source=b850b3a29a70c8eb888ce7dff776a5d1

【msdq】https://www.bilibili.com/video/BV1ez42197Zd/?spm_id_from=333.1007.tianma.1-1-1.click&vd_source=b850b3a29a70c8eb888ce7dff776a5d1

[计算机组成原理]

https://www.bilibili.com/video/BV1vt421L7oc/?spm_id_from=333.1007.tianma.2-1-4.click&vd_source=b850b3a29a70c8eb888ce7dff776a5d1

https://easydoc.net/s/78237135/ZUqEdvA4/HqQGp9TI



[Elasticsearch-Rest-High-Level-Client官网文档](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-search.html)

前端用户密码：admin/admin

- SpringBoot 2.7.8

- SpringCloud 2021.0.9
- SpringCloudAlibaba套装

![image-20240303202236782](images/image-20240303202236782.png)



# Elasticsearch的Mapping

- product-mapping

```bash
PUT product
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  },
  "mappings": {
    "properties": {
      "skuId": {
        "type": "long"
      },
      "spuId": {
        "type": "keyword"
      },
      "skuTitle": {
        "type": "text",
        "analyzer": "ik_smart"
      },
      "skuPrice": {
        "type": "double"
      },
      "skuImg": {
        "type": "keyword",
        "index": false,
        "doc_values": false
      },
      "saleCount": {
        "type": "long"
      },
      "hasStock": {
        "type": "boolean"
      },
      "hotScore": {
        "type": "long"
      },
      "brandId": {
        "type": "long"
      },
      "catalogId": {
        "type": "long"
      },
      "brandName": {
        "type": "keyword"
      },
      "brandImg": {
        "type": "keyword"
      },
      "catalogName": {
        "type": "keyword"
      },
      "attrs": {
        "type": "nested",
        "properties": {
          "attrId": {
            "type": "long"
          },
          "attrName": {
            "type": "keyword"
          },
          "attrValue": {
            "type": "keyword"
          }
        }
      }
    }
  }
}
```

- 商品的查询DSL

```bash
GET product/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "skuTitle": "华为"
          }
        }
      ],
      "filter": [
        {
          "term": {
            "catalogId": 225
          }
        },
        {
          "terms": {
            "brandId": [
              1,
              2,
              9
            ]
          }
        },
        {
          "nested": {
            "path": "attrs",
            "query": {
              "bool": {
                "must": [
                  {
                    "term": {
                      "attrs.attrId": 6
                    }
                  },
                  {
                    "terms": {
                      "attrs.attrValue": [
                        "32GB",
                        "64GB"
                      ]
                    }
                  }
                ]
              }
            }
          }
        },
        {
          "term": {
            "hasStock": true
          }
        },
        {
          "range": {
            "skuPrice": {
              "gte": 5000,
              "lte": 10000
            }
          }
        }
      ]
    }
  },
  "sort": [
    {
      "skuPrice": {
        "order": "desc"
      }
    }
  ],
  "from": 0,
  "size": 5,
  "highlight": {
    "fields": {
      "skuTitle": {}
    },
    "pre_tags": "<span color='red'>",
    "post_tags": "</span>"
  },
  "aggs": {
    "brand_agg": {
      "terms": {
        "field": "brandId",
        "size": 10
      },
      "aggs": {
        "brand_name_agg": {
          "terms": {
            "field": "brandName",
            "size": 10
          }
        },
        "brand_img_agg": {
          "terms": {
            "field": "brandImg",
            "size": 10
          }
        }
      }
    },
    "catalog_agg": {
      "terms": {
        "field": "catalogId",
        "size": 10
      },
      "aggs": {
        "catalog_name_agg": {
          "terms": {
            "field": "catalogName",
            "size": 10
          }
        }
      }
    },
    "attr_agg": {
      "nested": {
        "path": "attrs"
      },
      "aggs": {
        "attr_id_agg": {
          "terms": {
            "field": "attrs.attrId",
            "size": 10
          },
          "aggs": {
            "attr_name_agg": {
              "terms": {
                "field": "attrs.attrName",
                "size": 10
              }
            },
            "attr_value_agg":{
              "terms": {
                "field": "attrs.attrValue",
                "size": 10
              }
            }
          }
        }
      }
    }
  }
}
```



# Nginx配置

- 本地hosts

```bash
# 虚拟机ip
192.168.32.116		fsmall.com
192.168.32.116		search.fsmall.com
192.168.32.116		item.fsmall.com
192.168.32.116		auth.fsmall.com
192.168.32.116		cart.fsmall.com
192.168.32.116		order.fsmall.com
192.168.32.116		member.fsmall.com
```

- nginx.conf

```nginx
......
    #gzip  on;

    upstream fsmall {
	    # 本地ip
        server 192.168.32.1:88;
    }

    include /etc/nginx/conf.d/*.conf;
......
```

- fsmall.com

```nginx
# exp.mynatapp.cc内网穿透，转发到虚拟机ip的80端口
server {
    listen       80;
    server_name  fsmall.com *.fsmall.com exp.mynatapp.cc;

    #access_log  /var/log/nginx/host.access.log  main;

    # fullstack-*各个项目下的static（含）目录上传到nginx的html目录下，最终static下包含index/search/...
    location /static {
        root   /usr/share/nginx/html;
    }
    
    # 使用了内网穿透，添加订单域名，为了网关根据域名而负载均衡调用
    location /payed/notify {
        proxy_pass  http://fsmall;
        proxy_set_header Host order.fsmall.com;
    }
    location / {
        proxy_pass  http://fsmall;
        proxy_set_header Host $host;
    }
}
```

- exp.mynatapp.cc内网穿透配置图

![image-20240525191322065](images/image-20240525191322065.png)

# 消息队列流程

![image-20240516080913771](images/image-20240516080913771.png)

![image-20240516080922395](images/image-20240516080922395.png)

# 接入支付宝

[CSDN支付宝沙箱的使用](https://blog.csdn.net/qq_56282336/article/details/130845610)

[支付宝开放平台控制台沙箱](https://open.alipay.com/develop/sandbox/app)

[支付宝开放平台通用文档沙箱与秘钥工具](https://opendocs.alipay.com/common/02kipk?pathHash=0d20b438)

[异步通知说明文档](https://opendocs.alipay.com/open/270/105902?pathHash=d5cd617e)

[支付宝支付DEMO](https://opendocs.alipay.com/common/02kkv5?pathHash=17e7ce50)

[51IDEA运行支付宝支付DEMO](https://blog.51cto.com/wangzhenjun/5757130)



# 收单

1、订单在支付页，不支付，一直刷新，订单过期了才支付，订单状态改为已支付了，但是库存解锁了。

- 使用支付宝自动收单功能解决。只要一段时间不支付，就不能支付了。【推荐】

2、由于时延等问题。订单解锁完成，正在解锁库存的时候，异步通知才到。

- 订单解锁，手动调用收单。

3、网络阻塞问题，订单支付成功的异步通知一直不到达。

- 查询订单列表时，ajax获取当前未支付的订单状态，查询订单状态时，再获取一下支付宝此订单的状态。

4、 其他各种问题

- 每天晚上闲事下载支付宝对账单，——进行对账。





