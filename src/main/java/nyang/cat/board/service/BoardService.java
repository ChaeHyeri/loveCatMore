package nyang.cat.board.service;

import lombok.RequiredArgsConstructor;
import nyang.cat.board.dto.Pagination;
import nyang.cat.board.dto.SearchHandler;
import nyang.cat.board.entity.Board;
import nyang.cat.board.repository.BoardRepository;
import nyang.cat.Users.entity.Users;
import nyang.cat.Users.repository.UsersRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UsersRepository usersRepository;


    /* ------------------ 페이징 ------------------*/
    public Map getBoardList(Pageable pageable, SearchHandler sc, int pageNo, String category) {
        System.out.println("sc = " + sc);
        System.out.println("category = " + category);

        Map map = new HashMap();

        sc = Optional.ofNullable(sc).orElse(new SearchHandler());

        String option = sc.getOption();
        String keyword = sc.getKeyword();

        Page<Board> boardList = null;
        List<Board> listContent = null;

        if(category.equals("feeds")){
            boardList = boardRepository.findAll(pageable);
            listContent = boardList.getContent();
        }
        else if (option.equals("제목")) {
            boardList = boardRepository.findByTitleContaining(keyword, pageable);
            listContent = boardList.getContent();
        } else if (option.equals("제목 내용")) {
            System.out.println(" 제목+내용으로 검색 ");
            boardList = boardRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable);
            listContent = boardList.getContent();
        } else if (option.equals("작성자")) {
            System.out.println(" 작성자로 검색 ");
            boardList = boardRepository.findByWriterContaining(keyword, pageable);
            listContent = boardList.getContent();
        } else {
            boardList = boardRepository.findByCategory(category, pageable);
            listContent = boardList.getContent();

        }
        map.put("posts", listContent);


        int totalPage = boardList.getTotalPages();
        int startPage = (int) ((Math.floor((pageNo - 1) / 10) * 10) + 1
                <= totalPage ? (Math.floor((pageNo - 1) / 10) * 10) + 1 : totalPage);

        int endPage = (startPage + 10 - 1 < totalPage ? startPage + 10 - 1 : totalPage);
        System.out.println("endPage = " + endPage);

        boolean hasPrev = boardList.hasPrevious();
        boolean hasNext = boardList.hasNext();

        int prevIndex = boardList.previousOrFirstPageable().getPageNumber() + 1;
        int nextIndex = boardList.nextOrLastPageable().getPageNumber() + 1;

        map.put("pagination", new Pagination(totalPage, startPage, endPage, hasPrev, hasNext, prevIndex, nextIndex));
        System.out.println("페이지네이션=" + map.get("pagination"));

        return map;
    }

    /* ------------------ 글읽기 ------------------*/
    public Board findPost(Long pno) {
        Board board = boardRepository.findById(pno)
                .orElseThrow(() -> new RuntimeException(String.valueOf(pno)));

        /* 클릭하면 조회수 증가 */
        int viewCnt = board.getViewCnt();
        board.setViewCnt(viewCnt + 1);
        boardRepository.save(board);

        return board;
    }

    public Users getUserInfo(Authentication authentication) {
        Long username = Long.valueOf(authentication.getName());

        Optional<Users> findUser = usersRepository.findById(username);
        if (findUser.isPresent()) {
            Users user = findUser.get();
            return user;
        }
        throw new RuntimeException("회원정보를 찾을 수 없습니다.");
    }

    /* ------------------ 글쓰기 ------------------*/
    @Transactional
    public Board save(Authentication authentication, String title, String content, String category, MultipartFile file) throws IOException {
        Users user = getUserInfo(authentication);

        Board board = Board.builder()
                .title(title)
                .user(user)
                .writer(user.getName())
                .content(content)
                .category(category)
                .build();

        if(file != null) {
            /* 저장할 파일 이름 */
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            /* 저장할 경로 */
            String filePath = System.getProperty("user.dir") + "/src/main/resources/static/uploads/" + fileName;
            /* 파일 저장 */
            file.transferTo(new File(filePath));
            board.setImageName(fileName);
        }

        return boardRepository.save(board);

    }

    /* ------------------ 글삭제 ------------------*/
    public boolean delete(Authentication authentication, Long pno) {
        Users user = getUserInfo(authentication);
        String requestUser = user.getEmail();
        String writer = null;

        Optional<Board> findPost = boardRepository.findById(pno);
        if (findPost.isPresent()) {
            Board board = findPost.get();
            writer = board.getUser().getEmail();
        }
        if (requestUser.equals(writer)) {
            boardRepository.deleteById(pno);
            return true;
        }
        return false;
    }

    /* ------------------ 글수정 ------------------*/
    public boolean modify(Board board, Authentication authentication) {
        Users user = getUserInfo(authentication);

        String requestUser = user.getEmail();
        String writer = board.getUser().getEmail();

        if (requestUser.equals(writer)) {
            boardRepository.save(board);
            return true;
        }
        return false;
    }

}