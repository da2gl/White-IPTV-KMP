import clsx from "clsx";
import svgPaths from "./svg-h3rw4rcfcw";
type WrapperProps = {
  additionalClassNames?: string;
};

function Wrapper({ children, additionalClassNames = "" }: React.PropsWithChildren<WrapperProps>) {
  return (
    <div className={clsx("h-[48px] min-w-[84px] relative rounded-[8px] shrink-0 w-full", additionalClassNames)}>
      <div className="flex flex-row items-center justify-center min-w-[inherit] overflow-clip rounded-[inherit] size-full">
        <div className="content-stretch flex items-center justify-center min-w-[inherit] px-[20px] relative size-full">{children}</div>
      </div>
    </div>
  );
}

function OrSeparatorHorizontalDivider() {
  return (
    <div className="flex-[1_0_0] h-px min-h-px min-w-px relative">
      <div aria-hidden="true" className="absolute border-[#1e293b] border-solid border-t inset-0 pointer-events-none" />
    </div>
  );
}

export default function UploadIptvPlaylist() {
  return (
    <div className="bg-[#101c22] content-stretch flex flex-col items-start relative size-full" data-name="Upload IPTV Playlist">
      <div className="relative shrink-0 w-full" data-name="Container">
        <div className="flex flex-col items-center justify-center size-full">
          <div className="content-stretch flex flex-col items-center justify-center px-[16px] py-[100.5px] relative w-full">
            <div className="content-stretch flex flex-col gap-[24px] items-center max-w-[448px] relative shrink-0 w-full" data-name="Container">
              <div className="content-stretch flex flex-col gap-[12px] items-center justify-center py-[24px] relative shrink-0" data-name="Header: Logo/App Name">
                <div className="relative shrink-0 size-[40px]" data-name="Container">
                  <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 40 40">
                    <g id="Container">
                      <path d={svgPaths.p1cfdbb00} fill="var(--fill-0, #2BADEE)" id="Icon" />
                    </g>
                  </svg>
                </div>
                <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Heading 2">
                  <div className="flex flex-col font-['Inter:Bold',sans-serif] font-bold h-[32px] justify-center leading-[0] not-italic relative shrink-0 text-[24px] text-white tracking-[-0.6px] w-[109.17px]">
                    <p className="leading-[32px]">Streamify</p>
                  </div>
                </div>
              </div>
              <div className="content-stretch flex flex-col gap-[8px] items-center relative shrink-0" data-name="Text Block">
                <div className="content-stretch flex flex-col items-center px-[49.22px] relative shrink-0" data-name="Heading 1">
                  <div className="flex flex-col font-['Inter:Bold',sans-serif] font-bold h-[80px] justify-center leading-[40px] not-italic relative shrink-0 text-[32px] text-center text-white tracking-[-0.8px] w-[259.56px]">
                    <p className="mb-0">Upload your IPTV</p>
                    <p>playlist</p>
                  </div>
                </div>
                <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#94a3b8] text-[16px] text-center w-[335.95px]">
                  <p className="leading-[24px]">Enter a link or choose a file to start watching</p>
                </div>
              </div>
              <div className="content-stretch flex flex-col gap-[16px] items-start pt-[16px] relative shrink-0 w-full" data-name="Input Section">
                <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Label - URL Input Field">
                  <div className="content-stretch flex flex-col items-start pb-[8px] relative shrink-0 w-full" data-name="Container">
                    <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium justify-center leading-[0] not-italic relative shrink-0 text-[#cbd5e1] text-[14px] w-full">
                      <p className="leading-[21px]">Playlist URL</p>
                    </div>
                  </div>
                  <div className="bg-[#192730] h-[46px] relative rounded-[8px] shrink-0 w-full" data-name="Input">
                    <div className="overflow-clip relative rounded-[inherit] size-full">
                      <div className="absolute bottom-[13px] content-stretch flex flex-col items-start left-[13px] overflow-clip pr-[264.48px] top-[13px]" data-name="Container">
                        <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal h-[20px] justify-center leading-[0] not-italic relative shrink-0 text-[#64748b] text-[16px] w-[67.52px]">
                          <p className="leading-[normal]">{`https://...`}</p>
                        </div>
                      </div>
                      <div className="absolute bottom-[13px] left-[13px] top-[13px] w-[332px]" data-name="Container" />
                    </div>
                    <div aria-hidden="true" className="absolute border border-[#334155] border-solid inset-0 pointer-events-none rounded-[8px]" />
                  </div>
                </div>
                <div className="content-stretch flex items-center py-[8px] relative shrink-0 w-full" data-name="OR Separator">
                  <OrSeparatorHorizontalDivider />
                  <div className="content-stretch flex flex-col items-start px-[16px] relative shrink-0" data-name="Margin">
                    <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[16px] justify-center leading-[0] not-italic relative shrink-0 text-[#475569] text-[12px] uppercase w-[16.98px]">
                      <p className="leading-[16px]">Or</p>
                    </div>
                  </div>
                  <OrSeparatorHorizontalDivider />
                </div>
                <Wrapper additionalClassNames="bg-[rgba(43,173,238,0.2)]">
                  <div className="content-stretch flex flex-col items-center overflow-clip relative shrink-0" data-name="Container">
                    <div className="flex flex-col font-['Inter:Bold',sans-serif] font-bold h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#2badee] text-[16px] text-center tracking-[0.24px] w-[91.06px]">
                      <p className="leading-[24px]">Choose file</p>
                    </div>
                  </div>
                </Wrapper>
              </div>
              <div className="content-stretch flex flex-col gap-[16px] items-center pt-[16px] relative shrink-0 w-full" data-name="Primary CTA and Feedback">
                <Wrapper additionalClassNames="bg-[#2badee]">
                  <div className="content-stretch flex flex-col items-center overflow-clip relative shrink-0" data-name="Container">
                    <div className="flex flex-col font-['Inter:Bold',sans-serif] font-bold h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[16px] text-center text-white tracking-[0.24px] w-[115.67px]">
                      <p className="leading-[24px]">Import playlist</p>
                    </div>
                  </div>
                </Wrapper>
                <div className="content-stretch flex gap-[8px] h-[24px] items-center justify-center relative shrink-0 w-full" data-name="Feedback Area (Example: Error)">
                  <div className="relative shrink-0 size-[13.333px]" data-name="Container">
                    <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 13.3333 13.3333">
                      <g id="Container">
                        <path d={svgPaths.p12bd5ec0} fill="var(--fill-0, #F87171)" id="Icon" />
                      </g>
                    </svg>
                  </div>
                  <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                    <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal h-[20px] justify-center leading-[0] not-italic relative shrink-0 text-[#f87171] text-[14px] w-[140.39px]">
                      <p className="leading-[20px]">Invalid playlist format</p>
                    </div>
                  </div>
                </div>
                <div className="content-stretch flex flex-col items-start py-[8px] relative shrink-0" data-name="Tertiary Link">
                  <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[20px] justify-center leading-[0] not-italic relative shrink-0 text-[#2badee] text-[14px] w-[118.75px]">
                    <p className="leading-[20px]">Use demo playlist</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}