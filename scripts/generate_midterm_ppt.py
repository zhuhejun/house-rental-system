#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Generate a simple PPTX for the midterm presentation using only Python stdlib.
"""

from __future__ import annotations

from datetime import datetime, timezone
from pathlib import Path
from xml.sax.saxutils import escape
import zipfile


OUT_PATH = Path(__file__).resolve().parents[1] / "房屋租赁管理系统-中期验收汇报.pptx"

NS_A = "http://schemas.openxmlformats.org/drawingml/2006/main"
NS_R = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"
NS_P = "http://schemas.openxmlformats.org/presentationml/2006/main"

SLIDE_W = 12192000
SLIDE_H = 6858000


SLIDES = [
    {
        "title": "房屋租赁管理系统",
        "subtitle": "中期验收汇报",
        "lines": [
            "项目类型：基于 Vue + Spring Boot 的前后端分离系统",
            "汇报内容：项目目标、技术路线、阶段成果、当前问题与下一步计划",
            "日期：2026 年 4 月",
        ],
    },
    {
        "title": "项目背景与目标",
        "lines": [
            "• 面向房屋租赁业务，设计并实现一个完整的租赁管理平台。",
            "• 系统服务四类角色：游客、租客、房东、管理员。",
            "• 目标是打通房源浏览、预约看房、合同管理、账单支付、报修处理、退租管理等核心流程。",
            "• 在基础业务之上增加 AI 助手，提高找房与咨询体验。",
        ],
    },
    {
        "title": "技术路线与系统架构",
        "lines": [
            "• 前端：Vue2、Vue Router、Element UI、Axios。",
            "• 后端：Spring Boot、MyBatis-Plus、JWT。",
            "• 数据库：MySQL。",
            "• 其他：支付宝支付、ECharts 可视化、AI 助手。",
            "• 架构方式：前后端分离，前端通过 RESTful 接口调用后端服务。",
        ],
    },
    {
        "title": "目前已完成的主要功能",
        "lines": [
            "• 用户模块：注册、登录、身份校验、个人信息维护。",
            "• 游客模块：房源浏览、资讯浏览、搜索筛选、详情查看。",
            "• 房东模块：房东认证、发布房源、房源管理、流量查看。",
            "• 租赁流程：预约看房、发起合同、管理员审核、租客确认、押金支付。",
            "• 租后管理：租赁账单、报修工单、退租申请。",
            "• 后台管理：用户、房源、小区、公告、评论等管理功能。",
        ],
    },
    {
        "title": "核心业务流程",
        "lines": [
            "看房 -> 预约看房 -> 房东发起合同 -> 管理员审核 -> 租客确认并支付押金",
            "-> 合同生效 -> 账单管理 / 报修处理 / 退租流程",
            "",
            "• 当前系统已形成较完整的租赁业务闭环。",
            "• 支持多角色分工协作，业务状态流转较清晰。",
        ],
    },
    {
        "title": "数据库与后端设计",
        "lines": [
            "• 核心数据表：user、landlord、house、house_order_info、rental_contract。",
            "• 租后相关表：rental_bill、repair_order、rental_termination、payment_order。",
            "• 后端采用 controller -> service -> mapper 分层结构。",
            "• 使用 JWT 进行身份认证与角色权限区分。",
            "• 统一响应格式，便于前后端联调与错误处理。",
        ],
    },
    {
        "title": "中期完成情况与存在问题",
        "lines": [
            "• 已完成整体框架搭建与主要功能联调，项目已具备演示基础。",
            "• 当前整体开发进度约为 70% - 80%。",
            "• 已完成核心业务模块，但部分页面样式和交互细节仍可优化。",
            "• 支付、AI、内容审核等功能仍需进一步完善与测试。",
            "• 还需要补充边界场景验证、异常处理与性能优化。",
        ],
    },
    {
        "title": "下一阶段计划",
        "lines": [
            "• 完善剩余页面与交互细节，提升整体使用体验。",
            "• 加强前后端联调，补充异常与边界场景测试。",
            "• 优化支付、AI 助手与内容审核相关模块。",
            "• 完成系统测试、验收材料整理和最终答辩准备。",
            "",
            "感谢老师聆听，请老师批评指正。",
        ],
    },
]


def p(tag: str) -> str:
    return f"{{{NS_P}}}{tag}"


def a(tag: str) -> str:
    return f"{{{NS_A}}}{tag}"


def text_paragraph_xml(text: str, size: int = 2200, bold: bool = False, color: str | None = None) -> str:
    rpr = [f'lang="zh-CN"', f'sz="{size}"']
    if bold:
        rpr.append('b="1"')
    if color:
        color_xml = f"<a:solidFill><a:srgbClr val=\"{color}\"/></a:solidFill>"
    else:
        color_xml = ""
    end_rpr = f'lang="zh-CN" sz="{size}"'
    if bold:
        end_rpr += ' b="1"'
    return (
        "<a:p>"
        "<a:pPr algn=\"l\"/>"
        f"<a:r><a:rPr {' '.join(rpr)}>{color_xml}</a:rPr><a:t>{escape(text)}</a:t></a:r>"
        f"<a:endParaRPr {end_rpr}/>"
        "</a:p>"
    )


def textbox_xml(shape_id: int, name: str, x: int, y: int, cx: int, cy: int, paragraphs: list[str]) -> str:
    return f"""
    <p:sp>
      <p:nvSpPr>
        <p:cNvPr id="{shape_id}" name="{escape(name)}"/>
        <p:cNvSpPr txBox="1"/>
        <p:nvPr/>
      </p:nvSpPr>
      <p:spPr>
        <a:xfrm>
          <a:off x="{x}" y="{y}"/>
          <a:ext cx="{cx}" cy="{cy}"/>
        </a:xfrm>
        <a:prstGeom prst="rect"><a:avLst/></a:prstGeom>
        <a:noFill/>
        <a:ln><a:noFill/></a:ln>
      </p:spPr>
      <p:txBody>
        <a:bodyPr wrap="square" rtlCol="0"><a:spAutoFit/></a:bodyPr>
        <a:lstStyle/>
        {''.join(paragraphs)}
      </p:txBody>
    </p:sp>
    """


def banner_xml(shape_id: int, x: int, y: int, cx: int, cy: int, color: str) -> str:
    return f"""
    <p:sp>
      <p:nvSpPr>
        <p:cNvPr id="{shape_id}" name="Banner {shape_id}"/>
        <p:cNvSpPr/>
        <p:nvPr/>
      </p:nvSpPr>
      <p:spPr>
        <a:xfrm>
          <a:off x="{x}" y="{y}"/>
          <a:ext cx="{cx}" cy="{cy}"/>
        </a:xfrm>
        <a:prstGeom prst="rect"><a:avLst/></a:prstGeom>
        <a:solidFill><a:srgbClr val="{color}"/></a:solidFill>
        <a:ln><a:noFill/></a:ln>
      </p:spPr>
      <p:style>
        <a:lnRef idx="1"><a:schemeClr val="accent1"/></a:lnRef>
        <a:fillRef idx="1"><a:schemeClr val="accent1"/></a:fillRef>
        <a:effectRef idx="0"><a:schemeClr val="accent1"/></a:effectRef>
        <a:fontRef idx="minor"><a:schemeClr val="tx1"/></a:fontRef>
      </p:style>
      <p:txBody><a:bodyPr/><a:lstStyle/><a:p/></p:txBody>
    </p:sp>
    """


def slide_xml(title: str, lines: list[str], subtitle: str | None = None, cover: bool = False) -> str:
    shapes = [
        banner_xml(2, 0, 0, SLIDE_W, 500000, "0F766E"),
        banner_xml(3, 0, SLIDE_H - 260000, SLIDE_W, 260000, "DCEFEA"),
    ]
    if cover:
        shapes.append(banner_xml(4, 700000, 1100000, 2400000, 220000, "DCEFEA"))
        title_paragraphs = [text_paragraph_xml(title, size=3000, bold=True, color="0F172A")]
        shapes.append(textbox_xml(5, "Title", 850000, 1500000, 9500000, 900000, title_paragraphs))
        if subtitle:
            sub_paragraphs = [text_paragraph_xml(subtitle, size=2200, bold=True, color="0F766E")]
            shapes.append(textbox_xml(6, "Subtitle", 850000, 2450000, 4500000, 500000, sub_paragraphs))
        body_paras = [text_paragraph_xml(line, size=1700, color="334155") for line in lines]
        shapes.append(textbox_xml(7, "Body", 900000, 3200000, 9600000, 1600000, body_paras))
    else:
        title_paragraphs = [text_paragraph_xml(title, size=2400, bold=True, color="0F172A")]
        shapes.append(textbox_xml(4, "Title", 700000, 650000, 10500000, 700000, title_paragraphs))
        body_paras = [text_paragraph_xml(line, size=1750, color="334155") for line in lines]
        shapes.append(textbox_xml(5, "Body", 900000, 1500000, 10200000, 4300000, body_paras))

    return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sld xmlns:a="{NS_A}" xmlns:r="{NS_R}" xmlns:p="{NS_P}">
  <p:cSld>
    <p:spTree>
      <p:nvGrpSpPr>
        <p:cNvPr id="1" name=""/>
        <p:cNvGrpSpPr/>
        <p:nvPr/>
      </p:nvGrpSpPr>
      <p:grpSpPr>
        <a:xfrm>
          <a:off x="0" y="0"/>
          <a:ext cx="0" cy="0"/>
          <a:chOff x="0" y="0"/>
          <a:chExt cx="0" cy="0"/>
        </a:xfrm>
      </p:grpSpPr>
      {''.join(shapes)}
    </p:spTree>
  </p:cSld>
  <p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr>
</p:sld>
"""


def slide_rels_xml() -> str:
    return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>
</Relationships>
"""


def content_types_xml(slide_count: int) -> str:
    slide_overrides = "\n".join(
        f'  <Override PartName="/ppt/slides/slide{i}.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>'
        for i in range(1, slide_count + 1)
    )
    return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
  <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
  <Override PartName="/ppt/presentation.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"/>
  <Override PartName="/ppt/presProps.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presProps+xml"/>
  <Override PartName="/ppt/viewProps.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.viewProps+xml"/>
  <Override PartName="/ppt/tableStyles.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.tableStyles+xml"/>
  <Override PartName="/ppt/slideMasters/slideMaster1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml"/>
  <Override PartName="/ppt/slideLayouts/slideLayout1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml"/>
  <Override PartName="/ppt/theme/theme1.xml" ContentType="application/vnd.openxmlformats-officedocument.theme+xml"/>
{slide_overrides}
</Types>
"""


def root_rels_xml() -> str:
    return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="ppt/presentation.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>
"""


def app_xml(slide_count: int) -> str:
    return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties"
            xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
  <Application>Microsoft Office PowerPoint</Application>
  <PresentationFormat>On-screen Show (16:9)</PresentationFormat>
  <Slides>{slide_count}</Slides>
  <Notes>0</Notes>
  <HiddenSlides>0</HiddenSlides>
  <MMClips>0</MMClips>
  <ScaleCrop>false</ScaleCrop>
  <HeadingPairs>
    <vt:vector size="2" baseType="variant">
      <vt:variant><vt:lpstr>Theme</vt:lpstr></vt:variant>
      <vt:variant><vt:i4>1</vt:i4></vt:variant>
    </vt:vector>
  </HeadingPairs>
  <TitlesOfParts>
    <vt:vector size="1" baseType="lpstr">
      <vt:lpstr>Office Theme</vt:lpstr>
    </vt:vector>
  </TitlesOfParts>
  <Company></Company>
  <LinksUpToDate>false</LinksUpToDate>
  <SharedDoc>false</SharedDoc>
  <HyperlinksChanged>false</HyperlinksChanged>
  <AppVersion>16.0000</AppVersion>
</Properties>
"""


def core_xml() -> str:
    now = datetime.now(timezone.utc).replace(microsecond=0).isoformat().replace("+00:00", "Z")
    return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties"
                   xmlns:dc="http://purl.org/dc/elements/1.1/"
                   xmlns:dcterms="http://purl.org/dc/terms/"
                   xmlns:dcmitype="http://purl.org/dc/dcmitype/"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <dc:title>房屋租赁管理系统中期验收汇报</dc:title>
  <dc:creator>OpenAI Codex</dc:creator>
  <cp:lastModifiedBy>OpenAI Codex</cp:lastModifiedBy>
  <dcterms:created xsi:type="dcterms:W3CDTF">{now}</dcterms:created>
  <dcterms:modified xsi:type="dcterms:W3CDTF">{now}</dcterms:modified>
</cp:coreProperties>
"""


def presentation_xml(slide_count: int) -> str:
    slide_ids = "\n".join(
        f'    <p:sldId id="{255 + i}" r:id="rId{i + 1}"/>' for i in range(1, slide_count + 1)
    )
    return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:presentation xmlns:a="{NS_A}" xmlns:r="{NS_R}" xmlns:p="{NS_P}" saveSubsetFonts="1" autoCompressPictures="1">
  <p:sldMasterIdLst>
    <p:sldMasterId id="2147483648" r:id="rId1"/>
  </p:sldMasterIdLst>
  <p:sldIdLst>
{slide_ids}
  </p:sldIdLst>
  <p:sldSz cx="{SLIDE_W}" cy="{SLIDE_H}"/>
  <p:notesSz cx="6858000" cy="9144000"/>
  <p:defaultTextStyle>
    <a:defPPr/>
    <a:lvl1pPr marL="0" algn="l"><a:defRPr sz="1800"/></a:lvl1pPr>
    <a:lvl2pPr marL="457200" algn="l"><a:defRPr sz="1600"/></a:lvl2pPr>
    <a:lvl3pPr marL="914400" algn="l"><a:defRPr sz="1400"/></a:lvl3pPr>
  </p:defaultTextStyle>
</p:presentation>
"""


def presentation_rels_xml(slide_count: int) -> str:
    slide_rels = "\n".join(
        f'  <Relationship Id="rId{i + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide" Target="slides/slide{i}.xml"/>'
        for i in range(1, slide_count + 1)
    )
    return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="slideMasters/slideMaster1.xml"/>
{slide_rels}
</Relationships>
"""


def pres_props_xml() -> str:
    return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:presentationPr xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
                  xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
                  xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:showPr browse="0" kiosk="0" penClr="0" presenterMode="1"/>
</p:presentationPr>
"""


def view_props_xml() -> str:
    return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:viewPr xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
          xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
          xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:normalViewPr>
    <p:restoredLeft sz="15620"/>
    <p:restoredTop sz="94660"/>
  </p:normalViewPr>
  <p:slideViewPr/>
  <p:notesTextViewPr/>
  <p:gridSpacing cx="78028800" cy="78028800"/>
</p:viewPr>
"""


def table_styles_xml() -> str:
    return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<a:tblStyleLst xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" def="{5C22544A-7EE6-4342-B048-85BDC9FD1C3A}"/>
"""


def slide_master_xml() -> str:
    return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldMaster xmlns:a="{NS_A}" xmlns:r="{NS_R}" xmlns:p="{NS_P}">
  <p:cSld name="Office Theme">
    <p:bg>
      <p:bgPr>
        <a:solidFill><a:schemeClr val="bg1"/></a:solidFill>
        <a:effectLst/>
      </p:bgPr>
    </p:bg>
    <p:spTree>
      <p:nvGrpSpPr>
        <p:cNvPr id="1" name=""/>
        <p:cNvGrpSpPr/>
        <p:nvPr/>
      </p:nvGrpSpPr>
      <p:grpSpPr>
        <a:xfrm>
          <a:off x="0" y="0"/>
          <a:ext cx="0" cy="0"/>
          <a:chOff x="0" y="0"/>
          <a:chExt cx="0" cy="0"/>
        </a:xfrm>
      </p:grpSpPr>
    </p:spTree>
  </p:cSld>
  <p:clrMap bg1="lt1" tx1="dk1" bg2="lt2" tx2="dk2" accent1="accent1" accent2="accent2" accent3="accent3" accent4="accent4" accent5="accent5" accent6="accent6" hlink="hlink" folHlink="folHlink"/>
  <p:sldLayoutIdLst>
    <p:sldLayoutId id="2147483649" r:id="rId1"/>
  </p:sldLayoutIdLst>
  <p:txStyles>
    <p:titleStyle>
      <a:lvl1pPr algn="l"><a:defRPr sz="3000" b="1"/></a:lvl1pPr>
    </p:titleStyle>
    <p:bodyStyle>
      <a:lvl1pPr marL="0" algn="l"><a:defRPr sz="1800"/></a:lvl1pPr>
      <a:lvl2pPr marL="457200" algn="l"><a:defRPr sz="1600"/></a:lvl2pPr>
      <a:lvl3pPr marL="914400" algn="l"><a:defRPr sz="1400"/></a:lvl3pPr>
    </p:bodyStyle>
    <p:otherStyle>
      <a:defPPr/>
    </p:otherStyle>
  </p:txStyles>
</p:sldMaster>
"""


def slide_master_rels_xml() -> str:
    return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme" Target="../theme/theme1.xml"/>
</Relationships>
"""


def slide_layout_xml() -> str:
    return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldLayout xmlns:a="{NS_A}" xmlns:r="{NS_R}" xmlns:p="{NS_P}" type="blank" preserve="1">
  <p:cSld name="Blank">
    <p:spTree>
      <p:nvGrpSpPr>
        <p:cNvPr id="1" name=""/>
        <p:cNvGrpSpPr/>
        <p:nvPr/>
      </p:nvGrpSpPr>
      <p:grpSpPr>
        <a:xfrm>
          <a:off x="0" y="0"/>
          <a:ext cx="0" cy="0"/>
          <a:chOff x="0" y="0"/>
          <a:chExt cx="0" cy="0"/>
        </a:xfrm>
      </p:grpSpPr>
    </p:spTree>
  </p:cSld>
  <p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr>
</p:sldLayout>
"""


def slide_layout_rels_xml() -> str:
    return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="../slideMasters/slideMaster1.xml"/>
</Relationships>
"""


def theme_xml() -> str:
    return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<a:theme xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" name="Midterm Theme">
  <a:themeElements>
    <a:clrScheme name="Midterm">
      <a:dk1><a:sysClr val="windowText" lastClr="000000"/></a:dk1>
      <a:lt1><a:sysClr val="window" lastClr="FFFFFF"/></a:lt1>
      <a:dk2><a:srgbClr val="0F172A"/></a:dk2>
      <a:lt2><a:srgbClr val="F8FAFC"/></a:lt2>
      <a:accent1><a:srgbClr val="0F766E"/></a:accent1>
      <a:accent2><a:srgbClr val="2563EB"/></a:accent2>
      <a:accent3><a:srgbClr val="D97706"/></a:accent3>
      <a:accent4><a:srgbClr val="DC2626"/></a:accent4>
      <a:accent5><a:srgbClr val="7C3AED"/></a:accent5>
      <a:accent6><a:srgbClr val="475569"/></a:accent6>
      <a:hlink><a:srgbClr val="2563EB"/></a:hlink>
      <a:folHlink><a:srgbClr val="7C3AED"/></a:folHlink>
    </a:clrScheme>
    <a:fontScheme name="Midterm">
      <a:majorFont>
        <a:latin typeface="Aptos"/>
        <a:ea typeface="Microsoft YaHei"/>
        <a:cs typeface=""/>
        <a:font script="Hans" typeface="Microsoft YaHei"/>
      </a:majorFont>
      <a:minorFont>
        <a:latin typeface="Aptos"/>
        <a:ea typeface="Microsoft YaHei"/>
        <a:cs typeface=""/>
        <a:font script="Hans" typeface="Microsoft YaHei"/>
      </a:minorFont>
    </a:fontScheme>
    <a:fmtScheme name="Midterm">
      <a:fillStyleLst>
        <a:solidFill><a:schemeClr val="phClr"/></a:solidFill>
        <a:solidFill><a:schemeClr val="accent1"/></a:solidFill>
        <a:solidFill><a:schemeClr val="accent2"/></a:solidFill>
      </a:fillStyleLst>
      <a:lnStyleLst>
        <a:ln w="6350" cap="flat" cmpd="sng" algn="ctr"><a:solidFill><a:schemeClr val="phClr"/></a:solidFill><a:prstDash val="solid"/></a:ln>
        <a:ln w="12700" cap="flat" cmpd="sng" algn="ctr"><a:solidFill><a:schemeClr val="phClr"/></a:solidFill><a:prstDash val="solid"/></a:ln>
        <a:ln w="19050" cap="flat" cmpd="sng" algn="ctr"><a:solidFill><a:schemeClr val="phClr"/></a:solidFill><a:prstDash val="solid"/></a:ln>
      </a:lnStyleLst>
      <a:effectStyleLst>
        <a:effectStyle><a:effectLst/></a:effectStyle>
        <a:effectStyle><a:effectLst/></a:effectStyle>
        <a:effectStyle><a:effectLst/></a:effectStyle>
      </a:effectStyleLst>
      <a:bgFillStyleLst>
        <a:solidFill><a:schemeClr val="bg1"/></a:solidFill>
        <a:solidFill><a:schemeClr val="bg2"/></a:solidFill>
        <a:solidFill><a:schemeClr val="lt1"/></a:solidFill>
      </a:bgFillStyleLst>
    </a:fmtScheme>
  </a:themeElements>
  <a:objectDefaults/>
  <a:extraClrSchemeLst/>
</a:theme>
"""


def write_pptx(path: Path) -> None:
    with zipfile.ZipFile(path, "w", compression=zipfile.ZIP_DEFLATED) as zf:
        zf.writestr("[Content_Types].xml", content_types_xml(len(SLIDES)))
        zf.writestr("_rels/.rels", root_rels_xml())
        zf.writestr("docProps/app.xml", app_xml(len(SLIDES)))
        zf.writestr("docProps/core.xml", core_xml())
        zf.writestr("ppt/presentation.xml", presentation_xml(len(SLIDES)))
        zf.writestr("ppt/_rels/presentation.xml.rels", presentation_rels_xml(len(SLIDES)))
        zf.writestr("ppt/presProps.xml", pres_props_xml())
        zf.writestr("ppt/viewProps.xml", view_props_xml())
        zf.writestr("ppt/tableStyles.xml", table_styles_xml())
        zf.writestr("ppt/slideMasters/slideMaster1.xml", slide_master_xml())
        zf.writestr("ppt/slideMasters/_rels/slideMaster1.xml.rels", slide_master_rels_xml())
        zf.writestr("ppt/slideLayouts/slideLayout1.xml", slide_layout_xml())
        zf.writestr("ppt/slideLayouts/_rels/slideLayout1.xml.rels", slide_layout_rels_xml())
        zf.writestr("ppt/theme/theme1.xml", theme_xml())
        for idx, slide in enumerate(SLIDES, start=1):
            zf.writestr(
                f"ppt/slides/slide{idx}.xml",
                slide_xml(
                    title=slide["title"],
                    lines=slide["lines"],
                    subtitle=slide.get("subtitle"),
                    cover=(idx == 1),
                ),
            )
            zf.writestr(f"ppt/slides/_rels/slide{idx}.xml.rels", slide_rels_xml())


def main() -> None:
    write_pptx(OUT_PATH)
    print(str(OUT_PATH))


if __name__ == "__main__":
    main()
