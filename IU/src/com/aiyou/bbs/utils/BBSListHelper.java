
package com.aiyou.bbs.utils;

import java.util.Arrays;
import java.util.List;

import com.aiyou.AiYouApplication;
import com.aiyou.R;
import com.aiyou.bbs.bean.Board;
import com.aiyou.bbs.bean.Mailbox;
import com.aiyou.bbs.bean.Mailbox.MailboxType;
import com.aiyou.bbs.bean.Refer;
import com.aiyou.bbs.bean.Refer.ReferType;
import com.aiyou.bbs.bean.Search;
import com.aiyou.bbs.bean.VoteList;
import com.aiyou.bbs.bean.VoteList.VoteType;
import com.aiyou.bbs.bean.Widget;

/**
 * BBSListActivity的辅助类，
 * 
 * @author sollian
 */
public class BBSListHelper {
    public enum BeanType {
        WIDGET, BOARD, REFER, SEARCH, VOTELIST, MAILBOX, COLLECT;
    };

    /**
     * 版面名称
     */
    public String title;

    /**
     * 版面标志
     */
    public BeanType beanType = BeanType.WIDGET;
    /**
     * 版面名称
     */
    public String boardName;
    /**
     * 提醒类型
     */
    public ReferType referType;
    /**
     * 搜索文本
     */
    public String searchTitle;
    /**
     * 是否是标题。true：是；false：是作者
     */
    public boolean isSearchTitle = true;
    /**
     * 投票类型
     */
    public VoteType voteType;
    /**
     * 信箱box
     */
    public MailboxType mailboxType;

    /**
     * 含Article数组
     */
    private Widget mWidget;
    private Board mBoard;
    private Refer mRefer;
    private Search mSearch;
    /**
     * 含Vote数组
     */
    private VoteList mVoteList;
    /**
     * 含mail数组
     */
    private Mailbox mMailbox;

    private static BBSListHelper mInstance;

    private BBSListHelper() {
        title = AiYouApplication.getInstance().getString(R.string.topten);
    }

    public static BBSListHelper getInstance() {
        if (mInstance == null) {
            mInstance = new BBSListHelper();
        }
        return mInstance;
    }

    public Widget getWidget() {
        return mWidget;
    }

    public Board getBoard() {
        return mBoard;
    }

    public Refer getRefer() {
        return mRefer;
    }

    public Search getSearch() {
        return mSearch;
    }

    public VoteList getVoteList() {
        return mVoteList;
    }

    public Mailbox getMailbox() {
        return mMailbox;
    }

    public void updateWidget(Widget widget) {
        this.mWidget = widget;
    }

    public void updateBoard(Board board) {
        this.mBoard = board;
        BBSManager.getInstance(AiYouApplication.getInstance()).setAllowAttachment(board.name,
                board.allow_attachment);
    }

    public void updateRefer(Refer refer) {
        this.mRefer = refer;
    }

    public void updateSearch(Search search) {
        this.mSearch = search;
    }

    public void updateVoteList(VoteList voteList) {
        this.mVoteList = voteList;
    }

    public void updateMailbox(Mailbox mailbox) {
        this.mMailbox = mailbox;
    }

    /**
     * 获取列表
     * 
     * @return
     */
    @SuppressWarnings("rawtypes")
    public List getList() {
        List list = null;
        switch (beanType) {
            case BOARD:
                if (mBoard != null && mBoard.articles != null) {
                    list = Arrays.asList(mBoard.articles);
                }
                break;
            case COLLECT:
                break;
            case MAILBOX:
                if (mMailbox != null && mMailbox.mails != null) {
                    list = Arrays.asList(mMailbox.mails);
                }
                break;
            case REFER:
                if (mRefer != null && mRefer.refers != null) {
                    list = Arrays.asList(mRefer.refers);
                }
                break;
            case SEARCH:
                if (mSearch != null && mSearch.articles != null) {
                    list = Arrays.asList(mSearch.articles);
                }
                break;
            case VOTELIST:
                if (mVoteList != null && mVoteList.votes != null) {
                    list = Arrays.asList(mVoteList.votes);
                }
                break;
            case WIDGET:
                if (mWidget != null && mWidget.articles != null) {
                    list = Arrays.asList(mWidget.articles);
                }
                break;
            default:
                break;
        }
        return list;
    }

    /**
     * 获取标题
     * 
     * @return
     */
    public String getTitle() {
        switch (beanType) {
            case BOARD:
                if (mBoard != null) {
                    return mBoard.description;
                }
                break;
            case COLLECT:
                break;
            case MAILBOX:
                if (mMailbox != null) {
                    return mMailbox.description;
                }
                break;
            case REFER:
                if (mRefer != null) {
                    return mRefer.description;
                }
                break;
            case SEARCH:
                break;
            case VOTELIST:
                break;
            case WIDGET:
                if (mWidget != null) {
                    return mWidget.title;
                }
                break;
            default:
                break;

        }
        return null;
    }

    /**
     * 获取总页数
     * 
     * @return
     */
    public int getPageTotal() {
        switch (beanType) {
            case BOARD:
                if (mBoard != null && mBoard.pagination != null) {
                    return mBoard.pagination.page_all_count;
                }
                break;
            case COLLECT:
                break;
            case MAILBOX:
                if (mMailbox != null && mMailbox.pagination != null) {
                    return mMailbox.pagination.page_all_count;
                }
                break;
            case REFER:
                if (mRefer != null && mRefer.pagination != null) {
                    return mRefer.pagination.page_all_count;
                }
                break;
            case SEARCH:
                if (mSearch != null && mSearch.pagination != null) {
                    return mSearch.pagination.page_all_count;
                }
                break;
            case VOTELIST:
                if (mVoteList != null && mVoteList.pagination != null) {
                    return mVoteList.pagination.page_all_count;
                }
                break;
            case WIDGET:
                break;
            default:
                break;

        }
        return 1;
    }

    /**
     * 获取当前页数
     * 
     * @return
     */
    public int getPageCurrent() {
        switch (beanType) {
            case BOARD:
                if (mBoard != null && mBoard.pagination != null) {
                    return mBoard.pagination.page_current_count;
                }
                break;
            case COLLECT:
                break;
            case MAILBOX:
                if (mMailbox != null && mMailbox.pagination != null) {
                    return mMailbox.pagination.page_current_count;
                }
                break;
            case REFER:
                if (mRefer != null && mRefer.pagination != null) {
                    return mRefer.pagination.page_current_count;
                }
                break;
            case SEARCH:
                if (mSearch != null && mSearch.pagination != null) {
                    return mSearch.pagination.page_current_count;
                }
                break;
            case VOTELIST:
                if (mVoteList != null && mVoteList.pagination != null) {
                    return mVoteList.pagination.page_current_count;
                }
                break;
            case WIDGET:
                break;
            default:
                break;

        }
        return 1;
    }
}
