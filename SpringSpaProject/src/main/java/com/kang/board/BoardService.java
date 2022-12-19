package com.kang.board;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
@Transactional
public class BoardService {
	
	PageVo pVo;
	
	@Autowired
	BoardMapper boardMapper;
	
	Object savePoint;
	
	@Autowired
	PlatformTransactionManager manager;
	
	TransactionStatus status;
	
	public List<BoardVo> select(PageVo pVo) {
		int totSize = boardMapper.totList(pVo);
		pVo.setTotSize(totSize);
		this.pVo = pVo;
		List<BoardVo> list = boardMapper.select(pVo);
		return list;
	}
	
	public List<BoardVo> board10() {
		List<BoardVo> list = boardMapper.board10();
		return list;
	}
	
	public BoardVo view(PageVo pVo, BoardVo bVo, String up) {
		//수정 후 상세보기 페이지가 다시 나왔을 때 조회수가 오르지 않게
		if(up != null && up.equals("up")) {
			boardMapper.hitUp(pVo.getSno());
		}
		
		bVo = boardMapper.view(pVo.getSno());
		bVo.setAttList(boardMapper.attList(pVo.getSno()));
//		List<AttVo> attList = boardMapper.attList(pVo.getSno());
//		bVo.setAttList(attList);
		
		return bVo;
	}
	
	public boolean delete(BoardVo bVo) {
		boolean b = true;
		// 자신의 글에 댓글이 있는지 판단하기
		// 같은 grp안에 자신의 seq보다 1더 큰 seq의 자료에서
		// deep이 자신 보다 큰것이 있으면 댓글이 있는 것임.
		int replCnt = boardMapper.replCheck(bVo);
		
		if(replCnt > 0) {
			b = false;
			return b;
		}
		
		status = manager.getTransaction(new DefaultTransactionDefinition());
		this.savePoint = status.createSavepoint();
		
		int cnt = boardMapper.delete(bVo);
		if(cnt < 1) {
			b = false;
		} else {
			// sno를 pSno로 바꾸어 첨부 테이블에서 첨부파일 목록 가져오기
			List<String> attList = boardMapper.delFileList(bVo.getSno());
			
			// 첨부 테이블 삭제
			if(attList.size() > 0) {
				cnt = boardMapper.attDeleteAll(bVo.getSno());
				
				if(cnt > 0) {
					// 첨부 파일 삭제
					if(attList.size() > 0) {
						String[] delList = attList.toArray(new String[0]);
						fileDelete(delList);
					}
				} else {
					b = false;
				}
			}
		}
		
		if(b) manager.commit(status);
		else  status.rollbackToSavepoint(savePoint);
		
		return b;
	}
	
	public void fileDelete(String[] delFiles) {
		for (String f : delFiles) {
			File file = new File(FileUploadController.path + f);
			if (file.exists())
				file.delete();
		}
	}
	
	public boolean insertR(BoardVo bVo) {
		boolean flag = true;

		status = manager.getTransaction(new DefaultTransactionDefinition());
		savePoint = status.createSavepoint();
		int cnt = boardMapper.insertR(bVo);
		if(cnt < 1) {
			status.rollbackToSavepoint(savePoint);
//			String[] delFiles = new String[bVo.getAttList().size()];
//			for(int i=0; i<bVo.getAttList().size(); i++) {
//				delFiles[i] = bVo.getAttList().get(i).getSysFile();
//			}
//			fileDelete(delFiles);
			flag = false;
		} else {
			manager.commit(status);
		}
		
		//글 작성 실패한 경우 업로드 된 파일 삭제
//		if(flag) {
//			manager.commit(status);
//		} else {
//			status.rollbackToSavepoint(savePoint);
//			String[] delFiles = new String[bVo.getAttList().size()];
//			for(int i=0; i<bVo.getAttList().size(); i++) {
//				delFiles[i] = bVo.getAttList().get(i).getSysFile();
//			}
//			fileDelete(delFiles);
//		}
		
		return flag;
	}
	
	public void insertAttList(List<AttVo> attList) {
		status = manager.getTransaction(new DefaultTransactionDefinition());
		savePoint = status.createSavepoint();
		
		int cnt = boardMapper.insertAttList(attList);
		if(cnt > 0) {
			manager.commit(status);
		} else {
			savePoint = status.createSavepoint();
		}
	}
	
	public boolean updateR(BoardVo bVo, String[] delFiles) {
		boolean flag = true;
		status = manager.getTransaction(new DefaultTransactionDefinition());
		savePoint = status.createSavepoint();
		
		int cnt = boardMapper.update(bVo);
		if (cnt < 1) {
			flag = false;
		} else if (bVo.getAttList().size() > 0) {
			int attCnt = boardMapper.attUpdate(bVo);
			if (attCnt < 1) flag = false;
		}

		if(flag) {
			manager.commit(status);
			
			if(delFiles != null && delFiles.length > 0) {
				// 첨부 파일 데이터 삭제
				cnt = boardMapper.attDelete(delFiles);
				
				if(cnt > 0) {
					fileDelete(delFiles); // 파일 삭제
				} else {
					flag = false;
				}
			}
		} else {
			savePoint = status.createSavepoint();
		}
		
		return flag;
	}
	
	public boolean replR(BoardVo bVo) {
		boolean b = true;
		status = manager.getTransaction(new DefaultTransactionDefinition());
		savePoint = status.createSavepoint();
		
		boardMapper.seqUp(bVo);
		int cnt = boardMapper.replR(bVo);
		if(cnt < 1) {
			status.rollbackToSavepoint(savePoint);
			b = false;
		} else {
			manager.commit(status);
		}
		
		return b;
	}
	
	public PageVo getpVo() { return pVo; }

}
