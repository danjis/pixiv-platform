package com.pixiv.artwork.controller;

import com.pixiv.artwork.dto.ArtworkDTO;
import com.pixiv.artwork.dto.ArtworkDetailDTO;
import com.pixiv.artwork.dto.CreateArtworkRequest;
import com.pixiv.artwork.dto.UpdateArtworkRequest;
import com.pixiv.artwork.service.ArtworkService;
import com.pixiv.artwork.service.ArtworkSearchService;
import com.pixiv.common.dto.PageResult;
import com.pixiv.common.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 浣滃搧鎺у埗鍣?
 * 
 * 澶勭悊浣滃搧鐩稿叧鐨?HTTP 璇锋眰
 */
@Tag(name = "浣滃搧绠＄悊", description = "浣滃搧鍙戝竷銆佹煡璇€佺紪杈戙€佸垹闄ょ瓑鎺ュ彛")
@RestController
@RequestMapping("/api/artworks")
public class ArtworkController {

        private static final Logger logger = LoggerFactory.getLogger(ArtworkController.class);

        @Autowired
        private ArtworkService artworkService;

        @Autowired
        private ArtworkSearchService artworkSearchService;

        @Value("${ai.service.url:http://localhost:8000}")
        private String aiServiceUrl;

        /**
         * 鍒涘缓浣滃搧锛堝彂甯冧綔鍝侊級
         * 
         * @param request  鍒涘缓浣滃搧璇锋眰
         * @param artistId 鐢诲笀 ID锛堜粠 JWT 浠ょ墝涓幏鍙栵紝杩欓噷鏆傛椂鐢ㄨ姹傚ご妯℃嫙锛?
         * @return 鍒涘缓鐨勪綔鍝佷俊鎭?
         */
        @Operation(summary = "鍙戝竷浣滃搧", description = "鐢诲笀鍙戝竷鏂颁綔鍝併€傞渶瑕佺敾甯堣韩浠借璇併€俓n\n" +
                        "**X-User-Id 璇存槑锛?*\n" +
                        "- 杩欐槸褰撳墠鐢ㄦ埛鐨?ID锛堝繀椤绘槸鐢诲笀锛塡n" +
                        "- 鍙互浠庣櫥褰曞搷搴旂殑 user.id 瀛楁鑾峰彇\n" +
                        "- 鎴栬€呰皟鐢ㄧ敤鎴锋湇鍔＄殑 GET /api/users/me 鎺ュ彛鏌ョ湅\n\n" +
                        "**鍙戝竷娴佺▼锛?*\n" +
                        "1. 鍏堣皟鐢ㄦ枃浠舵湇鍔′笂浼犲浘鐗囷紝鑾峰彇 imageUrl 鍜?thumbnailUrl\n" +
                        "2. 濉啓浣滃搧淇℃伅锛堟爣棰樸€佹弿杩般€佸浘鐗嘦RL銆佹爣绛剧瓑锛塡n" +
                        "3. 鎻愪氦鍙戝竷璇锋眰\n" +
                        "4. 绯荤粺浼氳嚜鍔ㄥ彂閫佸埌鏅鸿兘鎵撴爣闃熷垪杩涜 AI 鏍囩璇嗗埆")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "浣滃搧鍒涘缓鎴愬姛"),
                        @ApiResponse(responseCode = "400", description = "鍙傛暟閿欒鎴栫敤鎴蜂笉鏄敾甯?),
                        @ApiResponse(responseCode = "401", description = "鏈璇?),
                        @ApiResponse(responseCode = "500", description = "鏈嶅姟鍣ㄩ敊璇?)
        })
        @PostMapping
        public ResponseEntity<Result<ArtworkDTO>> createArtwork(
                        @Parameter(description = "浣滃搧淇℃伅", required = true) @Valid @RequestBody CreateArtworkRequest request,

                        @Parameter(description = "褰撳墠鐢ㄦ埛 ID锛堝繀椤绘槸鐢诲笀锛塡n\n" +
                                        "**濡備綍鑾峰彇锛?*\n" +
                                        "1. 鐧诲綍鍚庝粠鍝嶅簲鐨?user.id 鑾峰彇\n" +
                                        "2. 鎴栬皟鐢?GET /api/users/me 鏌ョ湅\n\n" +
                                        "**绀轰緥鍊硷細** 1", required = true, example = "1") @RequestHeader("X-User-Id") Long artistId) {

                logger.info("鏀跺埌鍒涘缓浣滃搧璇锋眰: artistId={}, title={}", artistId, request.getTitle());

                try {
                        ArtworkDTO artwork = artworkService.createArtwork(artistId, request);
                        logger.info("浣滃搧鍒涘缓鎴愬姛: artworkId={}", artwork.getId());

                        return ResponseEntity
                                        .status(HttpStatus.CREATED)
                                        .body(Result.success(artwork));

                } catch (Exception e) {
                        logger.error("浣滃搧鍒涘缓澶辫触: artistId={}, error={}", artistId, e.getMessage(), e);
                        return ResponseEntity
                                        .status(HttpStatus.BAD_REQUEST)
                                        .body(Result.error(e.getMessage()));
                }
        }

        /**
         * 鏍规嵁 ID 鑾峰彇浣滃搧璇︽儏
         * 
         * @param artworkId 浣滃搧 ID
         * @param userId    褰撳墠鐢ㄦ埛 ID锛堝彲閫夛紝鐢ㄤ簬鏌ヨ鐐硅禐鏀惰棌鐘舵€侊級
         * @return 浣滃搧璇︽儏
         */
        @Operation(summary = "鑾峰彇浣滃搧璇︽儏", description = "鏍规嵁浣滃搧 ID 鏌ヨ浣滃搧鐨勮缁嗕俊鎭紝鍖呮嫭鏍囩銆佺粺璁℃暟鎹瓑銆俓n\n" +
                        "**杩斿洖淇℃伅鍖呮嫭锛?*\n" +
                        "- 浣滃搧鍩烘湰淇℃伅锛堟爣棰樸€佹弿杩般€佸浘鐗囩瓑锛塡n" +
                        "- 鐢诲笀淇℃伅\n" +
                        "- 鏍囩鍒楄〃锛堝寘鍚墜鍔ㄦ爣绛惧拰 AI 鑷姩鏍囩锛塡n" +
                        "- 缁熻鏁版嵁锛堟祻瑙堥噺銆佺偣璧炴暟銆佹敹钘忔暟绛夛級\n\n" +
                        "**缂撳瓨鏈哄埗锛?*\n" +
                        "- 浣滃搧璇︽儏浼氱紦瀛?1 灏忔椂\n" +
                        "- 娴忚璁℃暟寮傛鏇存柊锛屼笉褰卞搷鍝嶅簲閫熷害")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "鏌ヨ鎴愬姛"),
                        @ApiResponse(responseCode = "404", description = "浣滃搧涓嶅瓨鍦?),
                        @ApiResponse(responseCode = "500", description = "鏈嶅姟鍣ㄩ敊璇?)
        })
        @GetMapping("/{artworkId:\\d+}")
        public ResponseEntity<Result<ArtworkDetailDTO>> getArtwork(
                        @Parameter(description = "浣滃搧 ID", required = true, example = "1") @PathVariable("artworkId") Long artworkId,

                        @Parameter(description = "褰撳墠鐢ㄦ埛 ID锛堝彲閫夛紝鐢ㄤ簬鏌ヨ鐐硅禐鏀惰棌鐘舵€侊級", example = "1") @RequestHeader(value = "X-User-Id", required = false) Long userId) {
                logger.info("鏀跺埌鑾峰彇浣滃搧璇︽儏璇锋眰: artworkId={}, userId={}", artworkId, userId);

                try {
                        ArtworkDetailDTO artwork = artworkService.getArtwork(artworkId, userId);
                        return ResponseEntity.ok(Result.success(artwork));

                } catch (Exception e) {
                        logger.error("鑾峰彇浣滃搧璇︽儏澶辫触: artworkId={}, error={}", artworkId, e.getMessage(), e);
                        return ResponseEntity
                                        .status(HttpStatus.NOT_FOUND)
                                        .body(Result.error(e.getMessage()));
                }
        }

        /**
         * 鑾峰彇浣滃搧鍒楄〃锛堟敮鎸佸垎椤点€佹悳绱€佺瓫閫夈€佹帓搴忥級
         * 
         * @param keyword 鎼滅储鍏抽敭璇嶏紙鍙€夛級
         * @param tags    鏍囩鍒楄〃锛堝彲閫夛級
         * @param sortBy  鎺掑簭鏂瑰紡锛堝彲閫夛級
         * @param page    椤电爜锛堜粠 1 寮€濮嬶級
         * @param size    姣忛〉澶у皬
         * @return 浣滃搧鍒楄〃
         */
        @Operation(summary = "鑾峰彇浣滃搧鍒楄〃", description = "鏌ヨ浣滃搧鍒楄〃锛屾敮鎸佸垎椤点€佹悳绱€佺瓫閫夊拰鎺掑簭銆俓n\n" +
                        "**鎼滅储鍔熻兘锛?*\n" +
                        "- keyword: 鎼滅储鏍囬鍜屾弿杩颁腑鍖呭惈鍏抽敭璇嶇殑浣滃搧\n\n" +
                        "**绛涢€夊姛鑳斤細**\n" +
                        "- tags: 鎸夋爣绛剧瓫閫夛紝鏀寔澶氫釜鏍囩锛堜氦闆嗭級\n\n" +
                        "**鎺掑簭鏂瑰紡锛?*\n" +
                        "- latest: 鏈€鏂板彂甯冿紙榛樿锛塡n" +
                        "- hottest: 鏈€鐑棬锛堟寜鐑害鍒嗘暟锛塡n" +
                        "- most_liked: 鏈€澶氱偣璧瀄n" +
                        "- most_favorited: 鏈€澶氭敹钘廫n" +
                        "- most_viewed: 鏈€澶氭祻瑙圽n\n" +
                        "**绀轰緥锛?*\n" +
                        "- 鎼滅储鍏抽敭璇? /api/artworks?keyword=椋庢櫙&page=1&size=20\n" +
                        "- 鎸夋爣绛剧瓫閫? /api/artworks?tags=鍔ㄦ极,灏戝コ&page=1&size=20\n" +
                        "- 鎸夌儹搴︽帓搴? /api/artworks?sortBy=hottest&page=1&size=20")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "鏌ヨ鎴愬姛"),
                        @ApiResponse(responseCode = "400", description = "鍙傛暟閿欒"),
                        @ApiResponse(responseCode = "500", description = "鏈嶅姟鍣ㄩ敊璇?)
        })
        @GetMapping
        public ResponseEntity<Result<PageResult<ArtworkDTO>>> getArtworks(
                        @Parameter(description = "鎼滅储鍏抽敭璇嶏紙鎼滅储鏍囬銆佹弿杩板拰鐢诲笀鍚嶏級", example = "椋庢櫙") @RequestParam(value = "keyword", required = false) String keyword,

                        @Parameter(description = "鏍囩鍒楄〃锛堝涓爣绛剧敤閫楀彿鍒嗛殧锛屽彇浜ら泦锛?, example = "鍔ㄦ极,灏戝コ") @RequestParam(value = "tags", required = false) List<String> tags,

                        @RequestParam(value = "tags[]", required = false) List<String> tagsArray,

                        @Parameter(description = "鎺掑簭鏂瑰紡", example = "latest", schema = @io.swagger.v3.oas.annotations.media.Schema(allowableValues = {
                                        "latest", "hottest", "most_liked", "most_favorited",
                                        "most_viewed" })) @RequestParam(value = "sortBy", required = false, defaultValue = "latest") String sortBy,

                        @Parameter(description = "鐢诲笀ID锛堟寜鐢诲笀绛涢€夛級", example = "1") @RequestParam(value = "artistId", required = false) Long artistId,

                        @Parameter(description = "鏄惁AI鐢熸垚锛坱rue=浠匒I, false=浠呬汉宸? 涓嶄紶=鍏ㄩ儴锛?) @RequestParam(value = "isAigc", required = false) Boolean isAigc,

                        @Parameter(description = "寮€濮嬫棩鏈燂紙yyyy-MM-dd锛?, example = "2024-01-01") @RequestParam(value = "dateFrom", required = false) String dateFrom,

                        @Parameter(description = "缁撴潫鏃ユ湡锛坹yyy-MM-dd锛?, example = "2024-12-31") @RequestParam(value = "dateTo", required = false) String dateTo,

                        @Parameter(description = "鏈€浣庣偣璧炴暟", example = "10") @RequestParam(value = "minLikes", required = false) Integer minLikes,

                        @Parameter(description = "椤电爜锛堜粠 1 寮€濮嬶級", required = true, example = "1") @RequestParam(value = "page", defaultValue = "1") int page,

                        @Parameter(description = "姣忛〉澶у皬", required = true, example = "20") @RequestParam(value = "size", defaultValue = "20") int size) {

                List<String> normalizedTags = normalizeTags(tags, tagsArray);

                logger.info("鏀跺埌鑾峰彇浣滃搧鍒楄〃璇锋眰: keyword={}, tags={}, sortBy={}, artistId={}, isAigc={}, dateFrom={}, dateTo={}, minLikes={}, page={}, size={}",
                                keyword, normalizedTags, sortBy, artistId, isAigc, dateFrom, dateTo, minLikes, page, size);

                try {
                        // 鍙傛暟楠岃瘉
                        if (page < 1) {
                                return ResponseEntity
                                                .status(HttpStatus.BAD_REQUEST)
                                                .body(Result.error("椤电爜蹇呴』澶т簬 0"));
                        }
                        if (size < 1 || size > 100) {
                                return ResponseEntity
                                                .status(HttpStatus.BAD_REQUEST)
                                                .body(Result.error("姣忛〉澶у皬蹇呴』鍦?1-100 涔嬮棿"));
                        }

                        PageResult<ArtworkDTO> result = null;

                        // 浼樺厛浣跨敤 ES 鎼滅储锛堜粎褰撴湁鍏抽敭璇嶄笖鏃犻珮绾х瓫閫夋椂锛?
                        boolean useEs = keyword != null && !keyword.trim().isEmpty()
                                        && artistId == null && dateFrom == null && dateTo == null && minLikes == null;
                        if (useEs) {
                                result = artworkSearchService.search(keyword, normalizedTags, sortBy, page, size, isAigc);
                        }

                        // ES 涓嶅彲鐢ㄦ垨鏃犲叧閿瘝鏃讹紝闄嶇骇鍒?MySQL
                        if (result == null) {
                                result = artworkService.getArtworks(
                                                keyword, normalizedTags, sortBy, page, size, artistId, isAigc, dateFrom, dateTo,
                                                minLikes);
                        }

                        return ResponseEntity.ok(Result.success(result));

                } catch (Exception e) {
                        logger.error("鑾峰彇浣滃搧鍒楄〃澶辫触: error={}", e.getMessage(), e);
                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Result.error("鏌ヨ澶辫触: " + e.getMessage()));
                }
        }


        private List<String> normalizeTags(List<String> tags, List<String> tagsArray) {
                List<String> normalized = new java.util.ArrayList<>();
                if (tags != null) {
                        normalized.addAll(tags);
                }
                if (tagsArray != null) {
                        normalized.addAll(tagsArray);
                }
                return normalized.stream()
                                .filter(tag -> tag != null && !tag.trim().isEmpty())
                                .map(String::trim)
                                .distinct()
                                .toList();
        }
        /**
         * 鑾峰彇鍏虫敞鍔ㄦ€佹祦锛團eed锛?
         * 
         * @param userId 褰撳墠鐢ㄦ埛 ID锛堜粠璇锋眰澶磋幏鍙栵級
         * @param page   椤电爜锛堜粠 1 寮€濮嬶級
         * @param size   姣忛〉澶у皬
         * @return 鍏虫敞鐢诲笀鐨勪綔鍝佸垪琛?
         */
        @Operation(summary = "鑾峰彇鍏虫敞鍔ㄦ€?, description = "鑾峰彇褰撳墠鐢ㄦ埛鍏虫敞鐨勭敾甯堝彂甯冪殑浣滃搧锛屾寜鏃堕棿鍊掑簭銆俓n\n" +
                        "**鍔熻兘璇存槑锛?*\n" +
                        "- 闇€瑕佺櫥褰昞n" +
                        "- 杩斿洖鍏虫敞鐢诲笀鐨勬渶鏂颁綔鍝乗n" +
                        "- 鎸夊彂甯冩椂闂村€掑簭鎺掑垪\n" +
                        "- 鏀寔鍒嗛〉鏌ヨ")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "鏌ヨ鎴愬姛"),
                        @ApiResponse(responseCode = "401", description = "鏈璇?),
                        @ApiResponse(responseCode = "500", description = "鏈嶅姟鍣ㄩ敊璇?)
        })
        @GetMapping("/feed")
        public ResponseEntity<Result<PageResult<ArtworkDTO>>> getFeedArtworks(
                        @Parameter(description = "褰撳墠鐢ㄦ埛 ID", required = true, example = "1") @RequestHeader(value = "X-User-Id") Long userId,

                        @Parameter(description = "椤电爜锛堜粠 1 寮€濮嬶級", example = "1") @RequestParam(value = "page", defaultValue = "1") int page,

                        @Parameter(description = "姣忛〉澶у皬", example = "20") @RequestParam(value = "size", defaultValue = "20") int size) {

                logger.info("鏀跺埌鑾峰彇鍏虫敞鍔ㄦ€佽姹? userId={}, page={}, size={}", userId, page, size);

                try {
                        if (page < 1) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .body(Result.error("椤电爜蹇呴』澶т簬 0"));
                        }
                        if (size < 1 || size > 100) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .body(Result.error("姣忛〉澶у皬蹇呴』鍦?1-100 涔嬮棿"));
                        }

                        PageResult<ArtworkDTO> result = artworkService.getFeedArtworks(userId, page, size);
                        return ResponseEntity.ok(Result.success(result));

                } catch (Exception e) {
                        logger.error("鑾峰彇鍏虫敞鍔ㄦ€佸け璐? userId={}, error={}", userId, e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Result.error("鏌ヨ澶辫触: " + e.getMessage()));
                }
        }

        /**
         * 鑾峰彇鎺掕姒?
         * 
         * @param sortBy 鎺掑簭鏂瑰紡
         * @param period 鏃堕棿鑼冨洿
         * @param page   椤电爜锛堜粠 1 寮€濮嬶級
         * @param size   姣忛〉澶у皬
         * @return 鎺掕姒滀綔鍝佸垪琛?
         */
        @Operation(summary = "鑾峰彇鎺掕姒?, description = "鑾峰彇浣滃搧鎺掕姒滐紝鏀寔澶氱鎺掑簭鍜屾椂闂磋寖鍥淬€俓n\n" +
                        "**鎺掑簭鏂瑰紡锛?*\n" +
                        "- hottest: 鏈€鐑棬锛堟寜鐑害鍒嗘暟锛岄粯璁わ級\n" +
                        "- most_liked: 鏈€澶氱偣璧瀄n" +
                        "- most_favorited: 鏈€澶氭敹钘廫n" +
                        "- most_viewed: 鏈€澶氭祻瑙圽n\n" +
                        "**鏃堕棿鑼冨洿锛?*\n" +
                        "- day: 24灏忔椂鍐匼n" +
                        "- week: 涓€鍛ㄥ唴\n" +
                        "- month: 涓€涓湀鍐匼n" +
                        "- all: 鍏ㄩ儴鏃堕棿锛堥粯璁わ級")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "鏌ヨ鎴愬姛"),
                        @ApiResponse(responseCode = "400", description = "鍙傛暟閿欒"),
                        @ApiResponse(responseCode = "500", description = "鏈嶅姟鍣ㄩ敊璇?)
        })
        @GetMapping("/ranking")
        public ResponseEntity<Result<PageResult<ArtworkDTO>>> getRankingArtworks(
                        @Parameter(description = "鎺掑簭鏂瑰紡", example = "hottest", schema = @io.swagger.v3.oas.annotations.media.Schema(allowableValues = {
                                        "hottest", "most_liked", "most_favorited",
                                        "most_viewed" })) @RequestParam(value = "sortBy", defaultValue = "hottest") String sortBy,

                        @Parameter(description = "鏃堕棿鑼冨洿", example = "all", schema = @io.swagger.v3.oas.annotations.media.Schema(allowableValues = {
                                        "day", "week", "month",
                                        "all" })) @RequestParam(value = "period", defaultValue = "all") String period,

                        @Parameter(description = "椤电爜锛堜粠 1 寮€濮嬶級", example = "1") @RequestParam(value = "page", defaultValue = "1") int page,

                        @Parameter(description = "姣忛〉澶у皬", example = "50") @RequestParam(value = "size", defaultValue = "50") int size) {

                logger.info("鏀跺埌鑾峰彇鎺掕姒滆姹? sortBy={}, period={}, page={}, size={}", sortBy, period, page, size);

                try {
                        if (page < 1) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .body(Result.error("椤电爜蹇呴』澶т簬 0"));
                        }
                        if (size < 1 || size > 100) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .body(Result.error("姣忛〉澶у皬蹇呴』鍦?1-100 涔嬮棿"));
                        }

                        PageResult<ArtworkDTO> result = artworkService.getRankingArtworks(sortBy, period, page, size);
                        return ResponseEntity.ok(Result.success(result));

                } catch (Exception e) {
                        logger.error("鑾峰彇鎺掕姒滃け璐? error={}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Result.error("鏌ヨ澶辫触: " + e.getMessage()));
                }
        }

        /**
         * 鑾峰彇鐢ㄦ埛鏀惰棌鐨勪綔鍝佸垪琛?
         * 
         * @param userId 鐢ㄦ埛 ID锛堜粠璇锋眰澶磋幏鍙栵級
         * @param page   椤电爜锛堜粠 1 寮€濮嬶級
         * @param size   姣忛〉澶у皬
         * @return 鏀惰棌鐨勪綔鍝佸垪琛?
         */
        @Operation(summary = "鑾峰彇鐢ㄦ埛鏀惰棌鐨勪綔鍝佸垪琛?, description = "鏌ヨ褰撳墠鐢ㄦ埛鏀惰棌鐨勬墍鏈変綔鍝侊紝鎸夋敹钘忔椂闂村€掑簭鎺掑垪銆俓n\n" +
                        "**鍔熻兘璇存槑锛?*\n" +
                        "- 鍙兘鏌ヨ褰撳墠鐧诲綍鐢ㄦ埛鑷繁鐨勬敹钘忓垪琛╘n" +
                        "- 鎸夋敹钘忔椂闂村€掑簭鎺掑垪锛堟渶杩戞敹钘忕殑鍦ㄥ墠闈級\n" +
                        "- 鏀寔鍒嗛〉鏌ヨ\n\n" +
                        "**绀轰緥锛?*\n" +
                        "- GET /api/artworks/favorites?page=1&size=20")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "鏌ヨ鎴愬姛"),
                        @ApiResponse(responseCode = "400", description = "鍙傛暟閿欒"),
                        @ApiResponse(responseCode = "401", description = "鏈璇?),
                        @ApiResponse(responseCode = "500", description = "鏈嶅姟鍣ㄩ敊璇?)
        })
        @GetMapping("/favorites")
        public ResponseEntity<Result<PageResult<ArtworkDTO>>> getUserFavorites(
                        @Parameter(description = "褰撳墠鐢ㄦ埛 ID", required = true, example = "1") @RequestHeader(value = "X-User-Id") Long userId,

                        @Parameter(description = "椤电爜锛堜粠 1 寮€濮嬶級", required = true, example = "1") @RequestParam(value = "page", defaultValue = "1") int page,

                        @Parameter(description = "姣忛〉澶у皬", required = true, example = "20") @RequestParam(value = "size", defaultValue = "20") int size) {

                logger.info("鏀跺埌鑾峰彇鐢ㄦ埛鏀惰棌鍒楄〃璇锋眰: userId={}, page={}, size={}", userId, page, size);

                try {
                        // 鍙傛暟楠岃瘉
                        if (page < 1) {
                                return ResponseEntity
                                                .status(HttpStatus.BAD_REQUEST)
                                                .body(Result.error("椤电爜蹇呴』澶т簬 0"));
                        }
                        if (size < 1 || size > 100) {
                                return ResponseEntity
                                                .status(HttpStatus.BAD_REQUEST)
                                                .body(Result.error("姣忛〉澶у皬蹇呴』鍦?1-100 涔嬮棿"));
                        }

                        PageResult<ArtworkDTO> result = artworkService.getUserFavoriteArtworks(userId, page, size);
                        return ResponseEntity.ok(Result.success(result));

                } catch (Exception e) {
                        logger.error("鑾峰彇鐢ㄦ埛鏀惰棌鍒楄〃澶辫触: userId={}, error={}", userId, e.getMessage(), e);
                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Result.error("鏌ヨ澶辫触: " + e.getMessage()));
                }
        }

        /**
         * 鑾峰彇鐢诲笀鐨勪綔鍝佹暟閲忥紙鍐呴儴鏈嶅姟璋冪敤锛?
         *
         * @param artistId 鐢诲笀 ID
         * @return 浣滃搧鏁伴噺
         */
        @Operation(summary = "鑾峰彇鐢诲笀浣滃搧鏁伴噺", description = "鏌ヨ鎸囧畾鐢诲笀鐨勫凡鍙戝竷浣滃搧鏁伴噺銆俓n\n" +
                        "**鍔熻兘璇存槑锛?*\n" +
                        "- 浠呯粺璁″凡鍙戝竷鐘舵€佺殑浣滃搧\n" +
                        "- 鐢ㄤ簬鍐呴儴鏈嶅姟璋冪敤\n\n" +
                        "**绀轰緥锛?*\n" +
                        "- GET /api/artworks/count?artistId=1")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "鏌ヨ鎴愬姛"),
                        @ApiResponse(responseCode = "400", description = "鍙傛暟閿欒"),
                        @ApiResponse(responseCode = "500", description = "鏈嶅姟鍣ㄩ敊璇?)
        })
        @GetMapping("/count")
        public ResponseEntity<Result<Long>> getArtworkCount(
                        @Parameter(description = "鐢诲笀 ID", required = true, example = "1") @RequestParam(value = "artistId", required = true) Long artistId) {

                logger.info("鏀跺埌鑾峰彇浣滃搧鏁伴噺璇锋眰: artistId={}", artistId);

                try {
                        // 鍙傛暟楠岃瘉
                        if (artistId == null || artistId <= 0) {
                                logger.warn("鍙傛暟閿欒: artistId={}", artistId);
                                return ResponseEntity
                                                .status(HttpStatus.BAD_REQUEST)
                                                .body(Result.error("鐢诲笀 ID 涓嶈兘涓虹┖鎴栧皬浜庣瓑浜?0"));
                        }

                        long count = artworkService.getArtworkCountByArtist(artistId);
                        logger.info("鑾峰彇浣滃搧鏁伴噺鎴愬姛: artistId={}, count={}", artistId, count);
                        return ResponseEntity.ok(Result.success(count));
                } catch (Exception e) {
                        logger.error("鑾峰彇浣滃搧鏁伴噺澶辫触: artistId={}, error={}", artistId, e.getMessage(), e);
                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Result.error("鏌ヨ澶辫触: " + e.getMessage()));
                }
        }

        /**
         * 鏇存柊浣滃搧淇℃伅
         *
         * @param artworkId 浣滃搧 ID
         * @param artistId  褰撳墠鐢ㄦ埛 ID锛堢敾甯堬級
         * @param request   鏇存柊璇锋眰
         * @return 鏇存柊鍚庣殑浣滃搧淇℃伅
         */
        @Operation(summary = "缂栬緫浣滃搧", description = "缂栬緫宸插彂甯冪殑浣滃搧淇℃伅锛屽寘鎷爣棰樸€佹弿杩板拰鏍囩銆俓n\n" +
                        "**鏉冮檺瑕佹眰锛?*\n" +
                        "- 蹇呴』鏄綔鍝佺殑鍒涘缓鑰匼n\n" +
                        "**鍙紪杈戝瓧娈碉細**\n" +
                        "- title: 浣滃搧鏍囬\n" +
                        "- description: 浣滃搧鎻忚堪\n" +
                        "- tags: 鎵嬪姩鏍囩鍒楄〃锛堜細鏇挎崲鍘熸湁鎵嬪姩鏍囩锛孉I鏍囩淇濇寔涓嶅彉锛?)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "鏇存柊鎴愬姛"),
                        @ApiResponse(responseCode = "400", description = "鍙傛暟閿欒"),
                        @ApiResponse(responseCode = "403", description = "鏃犳潈缂栬緫姝や綔鍝?),
                        @ApiResponse(responseCode = "404", description = "浣滃搧涓嶅瓨鍦?),
                        @ApiResponse(responseCode = "500", description = "鏈嶅姟鍣ㄩ敊璇?)
        })
        @PutMapping("/{artworkId:\\d+}")
        public ResponseEntity<Result<ArtworkDTO>> updateArtwork(
                        @Parameter(description = "浣滃搧 ID", required = true, example = "1") @PathVariable("artworkId") Long artworkId,
                        @Parameter(description = "褰撳墠鐢ㄦ埛 ID锛堢敾甯堬級", required = true, example = "1") @RequestHeader(value = "X-User-Id") Long artistId,
                        @Valid @RequestBody UpdateArtworkRequest request) {

                logger.info("鏀跺埌鏇存柊浣滃搧璇锋眰: artworkId={}, artistId={}", artworkId, artistId);

                try {
                        ArtworkDTO artwork = artworkService.updateArtwork(artworkId, artistId, request);
                        logger.info("浣滃搧鏇存柊鎴愬姛: artworkId={}", artworkId);
                        return ResponseEntity.ok(Result.success(artwork));

                } catch (Exception e) {
                        logger.error("鏇存柊浣滃搧澶辫触: artworkId={}, error={}", artworkId, e.getMessage(), e);

                        if (e.getMessage().contains("涓嶅瓨鍦?)) {
                                return ResponseEntity
                                                .status(HttpStatus.NOT_FOUND)
                                                .body(Result.error(e.getMessage()));
                        } else if (e.getMessage().contains("鏃犳潈")) {
                                return ResponseEntity
                                                .status(HttpStatus.FORBIDDEN)
                                                .body(Result.error(e.getMessage()));
                        } else {
                                return ResponseEntity
                                                .status(HttpStatus.BAD_REQUEST)
                                                .body(Result.error("鏇存柊澶辫触: " + e.getMessage()));
                        }
                }
        }

        /**
         * 鍒犻櫎浣滃搧
         *
         * @param artworkId 浣滃搧 ID
         * @param artistId  褰撳墠鐢ㄦ埛 ID锛堢敾甯堬級
         * @return 鍒犻櫎缁撴灉
         */
        @Operation(summary = "鍒犻櫎浣滃搧", description = "鍒犻櫎鎸囧畾鐨勪綔鍝併€傚彧鏈変綔鍝佺殑鍒涘缓鑰呮墠鑳藉垹闄ゃ€俓n\n" +
                        "**鏉冮檺瑕佹眰锛?*\n" +
                        "- 蹇呴』鏄綔鍝佺殑鍒涘缓鑰匼n" +
                        "- 闇€瑕佺敾甯堣韩浠借璇乗n\n" +
                        "**娉ㄦ剰浜嬮」锛?*\n" +
                        "- 鍒犻櫎鎿嶄綔涓嶅彲鎭㈠\n" +
                        "- 浼氬悓鏃跺垹闄や綔鍝佺殑鎵€鏈夌浉鍏虫暟鎹紙鏍囩銆佽瘎璁恒€佺偣璧炪€佹敹钘忕瓑锛?)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "鍒犻櫎鎴愬姛"),
                        @ApiResponse(responseCode = "403", description = "鏃犳潈鍒犻櫎姝や綔鍝?),
                        @ApiResponse(responseCode = "404", description = "浣滃搧涓嶅瓨鍦?),
                        @ApiResponse(responseCode = "500", description = "鏈嶅姟鍣ㄩ敊璇?)
        })
        @DeleteMapping("/{artworkId:\\d+}")
        public ResponseEntity<Result<Void>> deleteArtwork(
                        @Parameter(description = "浣滃搧 ID", required = true, example = "1") @PathVariable("artworkId") Long artworkId,

                        @Parameter(description = "褰撳墠鐢ㄦ埛 ID锛堢敾甯堬級", required = true, example = "1") @RequestHeader(value = "X-User-Id") Long artistId) {

                logger.info("鏀跺埌鍒犻櫎浣滃搧璇锋眰: artworkId={}, artistId={}", artworkId, artistId);

                try {
                        artworkService.deleteArtwork(artworkId, artistId);
                        logger.info("浣滃搧鍒犻櫎鎴愬姛: artworkId={}", artworkId);
                        return ResponseEntity.ok(Result.success(null));

                } catch (Exception e) {
                        logger.error("鍒犻櫎浣滃搧澶辫触: artworkId={}, error={}", artworkId, e.getMessage(), e);

                        if (e.getMessage().contains("涓嶅瓨鍦?)) {
                                return ResponseEntity
                                                .status(HttpStatus.NOT_FOUND)
                                                .body(Result.error(e.getMessage()));
                        } else if (e.getMessage().contains("鏃犳潈")) {
                                return ResponseEntity
                                                .status(HttpStatus.FORBIDDEN)
                                                .body(Result.error(e.getMessage()));
                        } else {
                                return ResponseEntity
                                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .body(Result.error("鍒犻櫎澶辫触: " + e.getMessage()));
                        }
                }
        }

        /**
         * 鑾峰彇鐢ㄦ埛鑽夌鍒楄〃
         */
        @Operation(summary = "鑾峰彇鑽夌鍒楄〃", description = "鑾峰彇褰撳墠鐢ㄦ埛鐨勬墍鏈夎崏绋夸綔鍝?)
        @GetMapping("/drafts")
        public ResponseEntity<Result<PageResult<ArtworkDTO>>> getDrafts(
                        @RequestHeader("X-User-Id") Long artistId,
                        @RequestParam(value = "page", defaultValue = "1") int page,
                        @RequestParam(value = "size", defaultValue = "20") int size) {
                try {
                        PageResult<ArtworkDTO> result = artworkService.getDrafts(artistId, page, size);
                        return ResponseEntity.ok(Result.success(result));
                } catch (Exception e) {
                        logger.error("鑾峰彇鑽夌鍒楄〃澶辫触: error={}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Result.error("鏌ヨ澶辫触: " + e.getMessage()));
                }
        }

        /**
         * 鍙戝竷鑽夌
         */
        @Operation(summary = "鍙戝竷鑽夌", description = "灏嗚崏绋跨姸鎬佺殑浣滃搧鍙戝竷涓烘寮忎綔鍝?)
        @PutMapping("/{artworkId:\\d+}/publish")
        public ResponseEntity<Result<ArtworkDTO>> publishDraft(
                        @PathVariable("artworkId") Long artworkId,
                        @RequestHeader("X-User-Id") Long artistId) {
                try {
                        ArtworkDTO artwork = artworkService.publishDraft(artworkId, artistId);
                        return ResponseEntity.ok(Result.success(artwork));
                } catch (Exception e) {
                        logger.error("鍙戝竷鑽夌澶辫触: artworkId={}, error={}", artworkId, e.getMessage(), e);
                        if (e.getMessage().contains("涓嶅瓨鍦?)) {
                                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                                .body(Result.error(e.getMessage()));
                        }
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(Result.error(e.getMessage()));
                }
        }

        /**
         * 鎼滅储鑷姩琛ュ叏寤鸿
         */
        @Operation(summary = "鎼滅储寤鸿", description = "鏍规嵁鍏抽敭璇嶈繑鍥炲尮閰嶇殑鏍囩鍜屼綔鍝佹爣棰樺缓璁?)
        @GetMapping("/suggestions")
        public ResponseEntity<Result<java.util.List<ArtworkService.SearchSuggestionDTO>>> getSearchSuggestions(
                        @RequestParam("keyword") String keyword,
                        @RequestParam(value = "limit", defaultValue = "8") int limit) {
                try {
                        var suggestions = artworkService.getSearchSuggestions(keyword, limit);
                        return ResponseEntity.ok(Result.success(suggestions));
                } catch (Exception e) {
                        logger.error("鎼滅储寤鸿澶辫触: keyword={}, error={}", keyword, e.getMessage());
                        return ResponseEntity.ok(Result.success(java.util.Collections.emptyList()));
                }
        }

        // ==================== 浠ュ浘鎼滃浘 ====================

        /**
         * 浠ュ浘鎼滃浘
         *
         * 鐢ㄦ埛涓婁紶鍥剧墖 鈫?璋冪敤 AI 鏈嶅姟鎻愬彇鐗瑰緛鍚戦噺 鈫?ES kNN 鍚戦噺妫€绱?鈫?杩斿洖鐩镐技浣滃搧
         */
        @Operation(summary = "浠ュ浘鎼滃浘", description = "涓婁紶涓€寮犲浘鐗囷紝閫氳繃 AI 瑙嗚鐗瑰緛鍖归厤鏌ユ壘鐩镐技鐨勪綔鍝併€俓n\n"
                        + "**瀹炵幇鍘熺悊锛?*\n"
                        + "1. 涓婁紶鍥剧墖鍒?AI 鏈嶅姟鎻愬彇 DeepDanbooru 鐗瑰緛鍚戦噺\n"
                        + "2. 浣跨敤 Elasticsearch kNN 鍚戦噺妫€绱㈠尮閰嶇浉浼间綔鍝乗n"
                        + "3. 杩斿洖鎸夌浉浼煎害鎺掑簭鐨勪綔鍝佸垪琛╘n\n"
                        + "**鏀寔鏍煎紡锛?* JPG/PNG/GIF/WEBP锛屾渶澶?10MB")
        @PostMapping("/search-by-image")
        public ResponseEntity<Result<List<ArtworkDTO>>> searchByImage(
                        @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
                        @RequestParam(value = "size", defaultValue = "20") int size) {

                logger.info("鏀跺埌浠ュ浘鎼滃浘璇锋眰: fileName={}, fileSize={}", file.getOriginalFilename(),
                                file.getSize());

                try {
                        // 鍙傛暟楠岃瘉
                        if (file.isEmpty()) {
                                return ResponseEntity.badRequest().body(Result.error("璇蜂笂浼犲浘鐗囨枃浠?));
                        }
                        if (file.getSize() > 10 * 1024 * 1024) {
                                return ResponseEntity.badRequest().body(Result.error("鏂囦欢澶у皬涓嶈兘瓒呰繃 10MB"));
                        }
                        if (size < 1 || size > 50) {
                                size = 20;
                        }

                        // 璋冪敤 AI 鏈嶅姟鎻愬彇鐗瑰緛鍚戦噺
                        float[] featureVector = extractFeatureFromAiService(file);
                        if (featureVector == null) {
                                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                                                .body(Result.error("AI 鐗瑰緛鎻愬彇鏈嶅姟鏆備笉鍙敤"));
                        }

                        // ES 鍚戦噺妫€绱?
                        List<ArtworkDTO> results = artworkSearchService.searchByImage(featureVector, size);
                        logger.info("浠ュ浘鎼滃浘瀹屾垚: 杩斿洖 {} 鏉＄粨鏋?, results.size());

                        return ResponseEntity.ok(Result.success(results));

                } catch (Exception e) {
                        logger.error("浠ュ浘鎼滃浘澶辫触: error={}", e.getMessage(), e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Result.error("鎼滅储澶辫触: " + e.getMessage()));
                }
        }

        /**
         * 璋冪敤 AI 鏈嶅姟鎻愬彇鍥剧墖鐗瑰緛鍚戦噺
         */
        private float[] extractFeatureFromAiService(org.springframework.web.multipart.MultipartFile file) {
                try {
                        // 鏋勫缓 multipart 璇锋眰鍙戦€佸埌 AI 鏈嶅姟
                        String url = aiServiceUrl + "/api/extract-features/upload";

                        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                        headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);

                        org.springframework.util.MultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
                        body.add("file", new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                                @Override
                                public String getFilename() {
                                        return file.getOriginalFilename();
                                }
                        });

                        org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, Object>> requestEntity = new org.springframework.http.HttpEntity<>(
                                        body, headers);

                        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
                        var response = restTemplate.postForObject(url, requestEntity, java.util.Map.class);

                        if (response != null && response.containsKey("feature_vector")) {
                                List<Number> vectorList = (List<Number>) response.get("feature_vector");
                                float[] vector = new float[vectorList.size()];
                                for (int i = 0; i < vectorList.size(); i++) {
                                        vector[i] = vectorList.get(i).floatValue();
                                }
                                return vector;
                        }
                        return null;

                } catch (Exception e) {
                        logger.error("璋冪敤 AI 鐗瑰緛鎻愬彇鏈嶅姟澶辫触: {}", e.getMessage());
                        return null;
                }
        }

}

